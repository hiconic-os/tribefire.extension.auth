
package tribefire.extension.auth.rbac_openapi.wire;

import tribefire.module.wire.contract.StandardTribefireModuleWireModule;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.extension.auth.rbac_openapi.wire.space.RbacOpenapiModuleSpace;

public enum RbacOpenapiModuleWireModule implements StandardTribefireModuleWireModule {

	INSTANCE;

	@Override
	public Class<? extends TribefireModuleContract> moduleSpaceClass() {
		return RbacOpenapiModuleSpace.class;
	}

}