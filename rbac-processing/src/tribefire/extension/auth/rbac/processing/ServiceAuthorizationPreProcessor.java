package tribefire.extension.auth.rbac.processing;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.service.api.ReasonedServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.usersession.UserSession;

import tribefire.extension.auth.rbac.model.meta.AllowRoles;
import tribefire.extension.auth.rbac.model.meta.DenyRoles;

public class ServiceAuthorizationPreProcessor implements ReasonedServicePreProcessor<AuthorizedRequest> {
	private static final Logger logger = Logger.getLogger(ServiceAuthorizationPreProcessor.class);
	private Function<String, CmdResolver> mdResolverLookup;
	private Set<String> bypassRoles = Collections.emptySet();
	
	@Required
	public void setMdResolverLookup(Function<String, CmdResolver> mdResolverLookup) {
		this.mdResolverLookup = mdResolverLookup;
	}
	
	@Configurable
	public void setBypassRoles(Set<String> bypassRoles) {
		this.bypassRoles = bypassRoles;
	}
	
	@Override
	public Maybe<? extends AuthorizedRequest> processReasoned(ServiceRequestContext requestContext, AuthorizedRequest request) {
		StatefulServiceAuthorization serviceAuthorization = new StatefulServiceAuthorization(requestContext, request);
		return serviceAuthorization.process();
	}
	
	private record RequestPriviledging(String domainId, AuthorizedRequest request, Set<String> allowRoles, Set<String> denyRoles) {
		public boolean isPriviledged() {
			return !allowRoles.isEmpty() || !denyRoles.isEmpty();
		}
		
		public String implicitAllowRole() {
			return "@allow-" + domainId + "-"+ request().entityType().getTypeSignature();
		}
		
		public String implicitDenyRole() {
			return "@deny-" + domainId + "-" + request().entityType().getTypeSignature();
		}
	}
	
	private class StatefulServiceAuthorization {
		private ServiceRequestContext context;
		private Set<String> effectiveRoles;
		private AuthorizedRequest request;
		private UserSession userSession;
		
		public StatefulServiceAuthorization(ServiceRequestContext context, AuthorizedRequest request) {
			this.context = context;
			this.request = request;
		}
		
		public UserSession getUserSession() {
			if (userSession == null) {
				userSession = context.getAttribute(UserSessionAspect.class);
			}

			return userSession;
		}
		
		private Set<String> getEffectiveRoles() {
			return getUserSession().getEffectiveRoles();
		}
		
		private String getUserId() {
			return getUserSession().getUser().getId();
		}
		
		private boolean hasAnyRole(Set<String> roles) {
			Set<String> effectiveRoles = getEffectiveRoles();
			for (String role : roles) {
				if (effectiveRoles.contains(role)) {
					return true;
				}
			}
			
			return false;
		}
		
		private Maybe<? extends AuthorizedRequest> logDeniedAndReturnForbidden(String requestInfo) {
			logger.warn("ACCESS DENIED [role-governed] " + requestInfo + ", ip: " + context.getRequestorAddress());
			return Reasons.build(Forbidden.T).text("Insufficient priviledges to execute this request.").toMaybe();
		}
		
		private RequestPriviledging determineAllowAndDenyRoles() {
			String domainId = context.getDomainId();
			CmdResolver mdResolver = mdResolverLookup.apply(domainId);

			EntityMdResolver entityMdResolver = mdResolver.getMetaData().entity(request);
			AllowRoles allowRolesMd = entityMdResolver.meta(AllowRoles.T).exclusive();
			DenyRoles denyRolesMd = entityMdResolver.meta(DenyRoles.T).exclusive();
			
			Set<String> allowRoles = allowRolesMd != null? allowRolesMd.getRoles(): Collections.emptySet();
			Set<String> denyRoles = denyRolesMd != null? denyRolesMd.getRoles(): Collections.emptySet();
			
			return new RequestPriviledging(domainId, request, allowRoles, denyRoles);
		}
		
		public Maybe<? extends AuthorizedRequest> process() {
			RequestPriviledging priviledging = determineAllowAndDenyRoles();
			
			String requestInfo = "user: " + getUserId() + ", request: " + request.entityType().getTypeSignature() + ", domain: " + priviledging.domainId();
			
			if (!priviledging.isPriviledged()) {
				logger.debug("ACCESS GRANTED [role-agnostic] " + requestInfo);
				return Maybe.complete(request);
			}
			
			if (hasAnyRole(bypassRoles)) {
				logger.debug("SUPERUSER ACCESS GRANTED [role-governed] " + requestInfo);
				return Maybe.complete(request);
			}

			Set<String> allowRoles = priviledging.allowRoles();
			if (!allowRoles.isEmpty() && !hasAnyRole(allowRoles) && !hasRole(priviledging.implicitAllowRole()))
				return logDeniedAndReturnForbidden(requestInfo);

			if (hasRole(priviledging.implicitDenyRole()))
				return logDeniedAndReturnForbidden(requestInfo);
			
			if (hasAnyRole(priviledging.denyRoles()))
				return logDeniedAndReturnForbidden(requestInfo);
			
			logger.debug("ACCESS GRANTED [role-governed] " + requestInfo);
			
			return Maybe.complete(request);
		}

		private boolean hasRole(String role) {
			return getEffectiveRoles().contains(role);
		}
	}
}