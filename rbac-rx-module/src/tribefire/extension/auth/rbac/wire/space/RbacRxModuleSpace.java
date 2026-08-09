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
import tribefire.extension.auth.rbac.model.api.ServiceAuthorizationReflectionRequest;
import tribefire.extension.auth.rbac.processing.ServiceAuthorizationContext;
import tribefire.extension.auth.rbac.processing.ServiceAuthorizationPreProcessor;
import tribefire.extension.auth.rbac.processing.ServiceAuthorizationReflectionProcessor;
import tribefire.extension.auth.rbac.processing.RoleInductionInterceptor;

@Managed
public class RbacRxModuleSpace implements RxModuleContract {

	@Import
	private RxPlatformContract platform;
	
	@Override
	public void configureModels(ModelConfigurations configurations) {
		ModelConfiguration configuration = configurations.byName(_RbacConfiguredApiModel_.name);
		configuration.bindInterceptor("service-auth").forType(AuthorizedRequest.T).bind(this::serviceAuthorizationPreProcessor);
		configuration.bindInterceptor("role-induction").forType(AuthorizedRequest.T).bind(this::roleInductionInterceptor);
		configuration.bindRequest(ServiceAuthorizationReflectionRequest.T, this::serviceAuthorizationReflectionProcessor);
	}

	@Managed
	private RoleInductionInterceptor roleInductionInterceptor() {
		RoleInductionInterceptor bean = new RoleInductionInterceptor();
		bean.setAuthorizationContext(serviceAuthorizationContext());
		return bean;
	}
	
	@Managed
	private ServiceAuthorizationReflectionProcessor serviceAuthorizationReflectionProcessor() {
		ServiceAuthorizationReflectionProcessor bean = new ServiceAuthorizationReflectionProcessor();
		bean.setAuthorizationContext(serviceAuthorizationContext());
		return bean;
	}
	
	@Managed
	private ServiceAuthorizationPreProcessor serviceAuthorizationPreProcessor() {
		ServiceAuthorizationPreProcessor bean = new ServiceAuthorizationPreProcessor();
		bean.setAuthorizationContext(serviceAuthorizationContext());
		return bean;
	}
	
	@Managed
	private ServiceAuthorizationContext serviceAuthorizationContext() {
		ServiceAuthorizationContext bean = new ServiceAuthorizationContext();
		ServiceDomains serviceDomains = platform.serviceDomains();
		
		bean.setMdResolverLookup(domainId -> serviceDomains.byId(domainId).contextCmdResolver());
		bean.setOverrideRoles(Collections.set("internal"));
		return bean;
	}

}
