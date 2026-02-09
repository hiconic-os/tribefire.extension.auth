package tribefire.extension.auth.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.auth.rbac.annotation.AllowRoles;

@AllowRoles("role2")
public interface ProcessWithRole2 extends TestRequest {

	EntityType<ProcessWithRole2> T = EntityTypes.T(ProcessWithRole2.class);
}
