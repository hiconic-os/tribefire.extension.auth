package tribefire.extension.auth.rbac.model.api.data;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface RbacRequestAuthorization extends RequestAuthorization {
	EntityType<RbacRequestAuthorization> T = EntityTypes.T(RbacRequestAuthorization.class);
	
	String overrideRoles = "overrideRoles";
	String allowRoles = "allowRoles";
	String denyRoles = "denyRoles";
	
	Set<String> getOverrideRoles();
	void setOverrideRoles(Set<String> overrideRoles);
	
	Set<String> getAllowRoles();
	void setAllowRoles(Set<String> allowRoles);
	
	Set<String> getDenyRoles();
	void setDenyRoles(Set<String> denyRoles);
}

