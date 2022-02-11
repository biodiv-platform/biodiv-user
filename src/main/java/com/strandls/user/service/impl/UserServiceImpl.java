/**
 * 
 */
package com.strandls.user.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.esmodule.ApiException;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.esmodule.pojo.MapDocument;
import com.strandls.user.Constants;
import com.strandls.user.dao.FirebaseDao;
import com.strandls.user.dao.FollowDao;
import com.strandls.user.dao.UserDao;
import com.strandls.user.dto.FirebaseDTO;
import com.strandls.user.es.utils.UserIndex;
import com.strandls.user.exception.UnAuthorizedUserException;
import com.strandls.user.pojo.FirebaseTokens;
import com.strandls.user.pojo.Follow;
import com.strandls.user.pojo.Location;
import com.strandls.user.pojo.Role;
import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserEsMapping;
import com.strandls.user.pojo.UserIbp;
import com.strandls.user.pojo.UserLocationInfo;
import com.strandls.user.pojo.requests.UserDetails;
import com.strandls.user.pojo.requests.UserEmailPreferences;
import com.strandls.user.pojo.requests.UserRoles;
import com.strandls.user.service.UserService;
import com.strandls.user.util.AuthUtility;
import com.strandls.user.util.NotificationScheduler;

import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 *
 */
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Inject
	private UserDao userDao;

	@Inject
	private FirebaseDao firebaseDao;

	@Inject
	private FollowDao followDao;

	@Inject
	private EsServicesApi esService;

	@Inject
	private Channel channel;

	@Inject
	private ObjectMapper om;

	@Override
	public User fetchUser(Long userId) {
		User user = userDao.findById(userId);
		if (user.getProfilePic() == null || user.getProfilePic().isEmpty())
			user.setProfilePic(user.getIcon());
		else if (user.getIcon() == null || user.getIcon().isEmpty())
			user.setIcon(user.getProfilePic());

		return user;
	}

	private Long validateUserForEdits(HttpServletRequest request, Long inputUserId) throws UnAuthorizedUserException {
		boolean isAdmin = false;
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		JSONArray roles = (JSONArray) profile.getAttribute("roles");
		if (roles.contains("ROLE_ADMIN"))
			isAdmin = true;

		Long profileId = Long.parseLong(profile.getId());

		if (inputUserId == null)
			return profileId;

		if (!isAdmin && !inputUserId.equals(profileId))
			throw new UnAuthorizedUserException("Only admin can edit other users");

		return inputUserId;
	}

	public User updateProfilePic(HttpServletRequest request, Long userId, String profilePic)
			throws UnAuthorizedUserException, ApiException {
		userId = validateUserForEdits(request, userId);
		User user = userDao.findById(userId);

		user.setProfilePic(profilePic);

		user = userDao.update(user);
		esUserUpdate(user, true);
		return user;
	}

	@Override
	public User updateUserDetails(HttpServletRequest request, UserDetails inputUser)
			throws UnAuthorizedUserException, ApiException {

		Long inputUserId = validateUserForEdits(request, inputUser.getId());
		User user = userDao.findById(inputUserId);

		user.setUserName(inputUser.getUserName());
		user.setName(inputUser.getName());
		user.setSexType(inputUser.getSexType());
		user.setOccupation(inputUser.getOccupation());
		user.setInstitution(inputUser.getInstitution());
		user.setLocation(inputUser.getLocation());
		user.setLatitude(inputUser.getLatitude());
		user.setLongitude(inputUser.getLongitude());
		user.setAboutMe(inputUser.getAboutMe());
		user.setWebsite(inputUser.getWebsite());
		// TODO : Species group and habitat id
		if (AuthUtility.isAdmin(request)) {
			user.setEmail(inputUser.getEmail());
			user.setMobileNumber(inputUser.getMobileNumber());
		}
		user = userDao.update(user);
		esUserUpdate(user, true);
		return user;
	}

	@Override
	public void esUserUpdate(User user, Boolean isUpdate) throws ApiException {

		UserEsMapping userMapping = new UserEsMapping(user.getId(), user.getName(), user.getProfilePic(),
				user.getInstitution(), user.getLastLoginDate(), user.getOccupation(), user.getAccountLocked(),
				user.getLanguageId(), user.getAccountExpired(), user.getLocation(), user.getDateCreated(),
				user.getMobileNumber(), user.getIdentificationMail(), user.getSexType(), user.getSendPushNotification(),
				user.getUserName(), user.getAboutMe(), user.getHideEmial(), user.getEmail());

		Location location = new Location(user.getLatitude(), user.getLongitude());
		UserLocationInfo locationInformation = new UserLocationInfo(user.getLocation(), location);
		Map<String, Object> doc = new HashMap<String, Object>();
		doc.put("user", userMapping);
		doc.put("locationInformation", locationInformation);

		if (!isUpdate) {
			MapDocument document = new MapDocument();

			try {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				om.setDateFormat(df);
				document.setDocument(om.writeValueAsString(doc));
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage());
			}
			esService.create(UserIndex.INDEX.getValue(), UserIndex.TYPE.getValue(), user.getId().toString(), document);

		}
		esService.update(UserIndex.INDEX.getValue(), UserIndex.TYPE.getValue(), user.getId().toString(), doc);
	}

	@Override
	public User updateEmailPreferences(HttpServletRequest request, UserEmailPreferences inputUser)
			throws UnAuthorizedUserException, ApiException {

		Long inputUserId = validateUserForEdits(request, inputUser.getId());
		User user = userDao.findById(inputUserId);

		user.setIdentificationMail(inputUser.getIdentificationMail());
		user.setSendNotification(inputUser.getSendNotification());
		user.setHideEmial(inputUser.getHideEmial());
		user = userDao.update(user);
		esUserUpdate(user, true);
		return user;
	}

	@Override
	public User unsubscribeByUserEmail(String email) throws Exception {

		User user = userDao.findByUserEmail(email);

		if (user == null) {
			throw new Exception("User not found");
		} else if (Boolean.FALSE.equals(user.getSendNotification())) {
			throw new Exception("User Already unsubscribed");
		}

		user = userDao.update(user);
		esUserUpdate(user, true);
		return user;
	}

	@Override
	public User updateRolesAndPermission(HttpServletRequest request, UserRoles inputUser)
			throws UnAuthorizedUserException, ApiException {

		Long inputUserId = validateUserForEdits(request, inputUser.getId());
		User user = userDao.findById(inputUserId);

		if (inputUser.getRoles() == null)
			return user;

		user.setEnabled(inputUser.getEnabled());
		user.setAccountExpired(inputUser.getAccountExpired());
		user.setAccountLocked(inputUser.getAccountLocked());
		user.setPasswordExpired(inputUser.getPasswordExpired());
		user.setRoles(inputUser.getRoles());
		user = userDao.update(user);
		esUserUpdate(user, true);
		return user;
	}

	@Override
	public UserIbp fetchUserIbp(Long userId) {
		User user = userDao.findById(userId);
		Set<Role> roles = user.getRoles();
		Boolean isAdmin = false;
		for (Role role : roles) {
			if (role.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
				isAdmin = true;
				break;
			}
		}
		UserIbp ibp = new UserIbp(user.getId(), user.getName(),
				user.getProfilePic() != null ? user.getProfilePic() : user.getIcon(), isAdmin);
		return ibp;
	}

	@Override
	public List<UserIbp> fetchUserIbpBulk(List<Long> userIds) {
		List<UserIbp> result = new ArrayList<UserIbp>();
		for (Long userId : userIds) {
			result.add(fetchUserIbp(userId));
		}
		return result;
	}

	@Override
	public User getUserByEmail(String userEmail) {
		return userDao.findByUserEmail(userEmail);
	}

	@Override
	public User getUserByMobile(String mobileNumber) {
		return userDao.findByUserMobile(mobileNumber);
	}

	@Override
	public User updateUser(User user) {
		return userDao.update(user);
	}

	@Override
	public User getUserByEmailOrMobile(String data) {
		return userDao.findByUserEmailOrMobile(data);
	}

	@Override
	public Follow fetchByFollowId(Long id) {
		Follow follow = followDao.findById(id);
		return follow;
	}

	@Override
	public Follow fetchByFollowObject(String objectType, Long objectId, Long authorId) {
		if (objectType.equalsIgnoreCase(Constants.OBSERVATION))
			objectType = Constants.SPECIES_PARTICIPATION_OBSERVATION;
		else if (objectType.equalsIgnoreCase(Constants.DOCUMENT))
			objectType = Constants.CONTENT_EML_DOCUMENT;
		else if (objectType.equalsIgnoreCase(Constants.SPECIES))
			objectType = Constants.SPECIES_SPECIES;
		Follow follow = followDao.findByObject(objectType, objectId, authorId);
		return follow;
	}

	@Override
	public List<Follow> fetchFollowByUser(Long authorId) {
		List<Follow> follows = followDao.findByUser(authorId);
		return follows;
	}

	@Override
	public Follow updateFollow(String objectType, Long objectId, Long userId) {
		if (objectType.equalsIgnoreCase(Constants.OBSERVATION))
			objectType = Constants.SPECIES_PARTICIPATION_OBSERVATION;
		else if (objectType.equalsIgnoreCase(Constants.DOCUMENT))
			objectType = Constants.CONTENT_EML_DOCUMENT;
		else if (objectType.equalsIgnoreCase(Constants.SPECIES))
			objectType = Constants.SPECIES_SPECIES;
		Follow follow = followDao.findByObject(objectType, objectId, userId);
		if (follow == null) {
			follow = new Follow(null, objectId, objectType, userId, new Date());
			follow = followDao.save(follow);

		}
		return follow;
	}

	@Override
	public Follow unFollow(String objectType, Long objectId, Long userId) {
		if (objectType.equalsIgnoreCase(Constants.OBSERVATION))
			objectType = Constants.SPECIES_PARTICIPATION_OBSERVATION;
		else if (objectType.equalsIgnoreCase(Constants.DOCUMENT))
			objectType = Constants.CONTENT_EML_DOCUMENT;
		else if (objectType.equalsIgnoreCase(Constants.SPECIES))
			objectType = Constants.SPECIES_SPECIES;
		Follow follow = followDao.findByObject(objectType, objectId, userId);
		if (follow != null) {
			follow = followDao.delete(follow);
		}
		return follow;
	}

	@Override
	public List<User> getNames(String name) {
		return userDao.findNames(name);
	}

	@Override
	public List<User> fetchRecipients(String objectType, Long objectId) {
		List<Follow> followers = followDao.findByObject(objectType, objectId);
		List<User> recipients = new ArrayList<>();
		if (followers != null) {
			for (Follow follower : followers) {
				User user = userDao.findById(follower.getAuthorId());
				if (user != null) {
					recipients.add(user);
				}
			}
		}
		return recipients;
	}

	@Override
	public FirebaseTokens saveToken(Long userId, String fcmToken) {
		FirebaseTokens token = firebaseDao.getToken(userId, fcmToken);
		try {
			if (token == null) {
				User user = fetchUser(userId);
				user.setSendPushNotification(true);
				updateUser(user);
				FirebaseTokens savedToken = new FirebaseTokens(user, fcmToken);
				token = firebaseDao.save(savedToken);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return token;
	}

	@Override
	public void sendPushNotifications(FirebaseDTO firebaseDTO) {
		List<FirebaseTokens> tokens = firebaseDao.findAll();
		NotificationScheduler scheduler = new NotificationScheduler(channel, firebaseDTO, tokens);
		scheduler.start();
	}

	@Override
	public String deleteUser(HttpServletRequest request, Long userId) {
		try {
			User user = fetchUser(userId);
			user.setIsDeleted(Boolean.TRUE);
			updateUser(user);
			esService.delete(UserIndex.INDEX.getValue(), UserIndex.TYPE.getValue(), userId.toString());
			return "deleted";
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}

	@Override
	public List<User> getAllAdmins() {
		List<Long> adminIdList = userDao.findRoleAdmin();
		List<User> result = new ArrayList<User>();
		for (Long adminId : adminIdList) {
			result.add(fetchUser(adminId));
		}
		return result;
	}

}
