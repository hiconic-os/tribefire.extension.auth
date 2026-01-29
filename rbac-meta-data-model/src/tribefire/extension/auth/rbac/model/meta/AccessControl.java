package tribefire.extension.auth.rbac.model.meta;

import java.util.Set;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.UniversalMetaData;

@Abstract
public interface AccessControl extends UniversalMetaData {

	EntityType<AccessControl> T = EntityTypes.T(AccessControl.class);
	
	String roles = "roles";
	
	Set<String> getRoles();
	void setRoles(Set<String> roles);
}
