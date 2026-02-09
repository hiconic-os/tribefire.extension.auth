package tribefire.extension.auth.rbac.model.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface AllowRoles extends AccessControl {
	EntityType<AllowRoles> T = EntityTypes.T(AllowRoles.class);
}
