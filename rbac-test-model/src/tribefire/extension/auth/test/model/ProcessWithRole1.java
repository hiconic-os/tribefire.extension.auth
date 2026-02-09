package tribefire.extension.auth.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.auth.rbac.annotation.AllowRoles;

@AllowRoles("role1")
public interface ProcessWithRole1 extends TestRequest {

	EntityType<ProcessWithRole1> T = EntityTypes.T(ProcessWithRole1.class);
}
