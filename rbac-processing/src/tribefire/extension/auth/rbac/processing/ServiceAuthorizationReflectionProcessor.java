package tribefire.extension.auth.rbac.processing;

import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.usersession.UserSession;

import tribefire.extension.auth.rbac.model.api.GetCurrentUserRequestAuthorizations;
import tribefire.extension.auth.rbac.model.api.GetRbacRequestAuthorizations;
import tribefire.extension.auth.rbac.model.api.ServiceAuthorizationReflectionRequest;
import tribefire.extension.auth.rbac.model.api.data.CurrentUserRequestAuthorization;
import tribefire.extension.auth.rbac.model.api.data.CurrentUserRequestAuthorizations;
import tribefire.extension.auth.rbac.model.api.data.RbacRequestAuthorization;
import tribefire.extension.auth.rbac.model.api.data.RbacRequestAuthorizations;

public class ServiceAuthorizationReflectionProcessor extends AbstractDispatchingServiceProcessor<ServiceAuthorizationReflectionRequest, Object> {
	
	private ServiceAuthorizationContext authorizationContext;
	
	@Required
	public void setAuthorizationContext(ServiceAuthorizationContext authorizationContext) {
		this.authorizationContext = authorizationContext;
	}
	
	@Override
	protected void configureDispatching( DispatchConfiguration<ServiceAuthorizationReflectionRequest, Object> dispatching) {
		dispatching.register(GetRbacRequestAuthorizations.T, this::getRbacRequestionAuthorizations);
		dispatching.register(GetCurrentUserRequestAuthorizations.T, this::getCurrentUserRequestionAuthorizations);
	}

	private RbacRequestAuthorizations getRbacRequestionAuthorizations(ServiceRequestContext context, GetRbacRequestAuthorizations request) {
		String domainId = context.getDomainId();
		ServiceDomainAuthorizationResolver resolver = authorizationContext.getDomainResolver(domainId);
		
		RbacRequestAuthorizations result = RbacRequestAuthorizations.T.create();
		List<RbacRequestAuthorization> authorizations = result.getAuthorizations();
		
		for (var authorization : resolver.resolveAll()) {
			RbacRequestAuthorization requestAuthorization = RbacRequestAuthorization.T.create();
			requestAuthorization.setType(authorization.type().getTypeSignature());
			requestAuthorization.setOverrideRoles(authorization.overrideRoles());
			requestAuthorization.getAllowRoles().addAll(authorization.allowRoles());
			requestAuthorization.getDenyRoles().addAll(authorization.denyRoles());
			authorizations.add(requestAuthorization);
		}
			
		return result;
	}
	
	private CurrentUserRequestAuthorizations getCurrentUserRequestionAuthorizations(ServiceRequestContext context, GetCurrentUserRequestAuthorizations request) {
		UserSession userSession = context.getAttribute(UserSessionAspect.class);
		Set<String> effectiveRoles = userSession.getEffectiveRoles();
		String domainId = context.getDomainId();
		ServiceDomainAuthorizationResolver resolver = authorizationContext.getDomainResolver(domainId);
		
		CurrentUserRequestAuthorizations result = CurrentUserRequestAuthorizations.T.create();
		List<CurrentUserRequestAuthorization> authorizations = result.getAuthorizations();
		
		for (var authorization : resolver.resolveAll()) {
			CurrentUserRequestAuthorization requestAuthorization = CurrentUserRequestAuthorization.T.create();
			requestAuthorization.setType(authorization.type().getTypeSignature());
			requestAuthorization.setAllowed(authorization.isAllowed(effectiveRoles));
			authorizations.add(requestAuthorization);
		}
			
		return result;
	}
}
