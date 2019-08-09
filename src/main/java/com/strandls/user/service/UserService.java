/**
 * 
 */
package com.strandls.user.service;

import com.strandls.user.pojo.User;
import com.strandls.user.pojo.UserIbp;

/**
 * @author Abhishek Rudra
 *
 */
public interface UserService {
	
	public User fetchUser(Long userId);
	
	public UserIbp fetchUserIbp(Long userId);

}
