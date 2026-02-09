package tribefire.extension.auth.rbac.wire.space;

import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import hiconic.rx.module.api.service.ModelConfiguration;
import hiconic.rx.module.api.service.ModelConfigurations;
import hiconic.rx.module.api.service.ServiceDomains;
import hiconic.rx.module.api.wire.RxModuleContract;
import hiconic.rx.module.api.wire.RxPlatformContract;
import jsinterop.utils.Collections;
import tribefire.extension.auth._RbacConfiguredApiModel_;
import tribefire.extension.auth.rbac.processing.ServiceAuthorizationPreProcessor;

@Managed
public class RbacRxModuleSpace implements RxModuleContract {

	@Import
	private RxPlatformContract platform;
	
	@Override
	public void configureModels(ModelConfigurations configurations) {
		ModelConfiguration configuration = configurations.byName(_RbacConfiguredApiModel_.name);
		configuration.bindInterceptor("service-auth").forType(AuthorizedRequest.T).bind(this::serviceAuthorizationPreProcessor);
	}
	
	@Managed
	private ServiceAuthorizationPreProcessor serviceAuthorizationPreProcessor() {
		ServiceAuthorizationPreProcessor bean = new ServiceAuthorizationPreProcessor();
		ServiceDomains serviceDomains = platform.serviceDomains();
		
		bean.setMdResolverLookup(domainId -> serviceDomains.byId(domainId).contextCmdResolver());
		bean.setBypassRoles(Collections.set("internal"));
		return bean;
	}

}