package tribefire.extension.auth.rbac.model.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface DeniedFor extends AccessControl {
	EntityType<DeniedFor> T = EntityTypes.T(DeniedFor.class);
}
