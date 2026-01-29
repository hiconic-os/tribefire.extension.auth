package tribefire.extension.auth.rbac.model.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface GrantedFor extends AccessControl {
	EntityType<GrantedFor> T = EntityTypes.T(GrantedFor.class);
}
