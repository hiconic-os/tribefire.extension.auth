
package tribefire.extension.auth.rbac.wire;

import tribefire.module.wire.contract.StandardTribefireModuleWireModule;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.extension.auth.rbac.wire.space.RbacModuleSpace;

public enum RbacModuleWireModule implements StandardTribefireModuleWireModule {

	INSTANCE;

	@Override
	public Class<? extends TribefireModuleContract> moduleSpaceClass() {
		return RbacModuleSpace.class;
	}

}