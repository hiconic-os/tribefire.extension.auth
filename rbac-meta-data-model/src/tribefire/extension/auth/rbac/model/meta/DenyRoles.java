package tribefire.extension.auth.rbac.model.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface DenyRoles extends AccessControl {
	EntityType<DenyRoles> T = EntityTypes.T(DenyRoles.class);
}
