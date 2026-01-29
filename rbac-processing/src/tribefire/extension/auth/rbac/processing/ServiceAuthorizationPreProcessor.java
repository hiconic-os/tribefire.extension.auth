package tribefire.extension.auth.rbac.processing;

import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.service.api.ReasonedServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.AuthorizedRequest;

import tribefire.extension.auth.rbac.model.meta.DeniedFor;
import tribefire.extension.auth.rbac.model.meta.GrantedFor;

public class ServiceAuthorizationPreProcessor implements ReasonedServicePreProcessor<AuthorizedRequest> {

	private Function<String, CmdResolver> mdResolverLookup;
	
	@Required
	public void setMdResolverLookup(Function<String, CmdResolver> mdResolverLookup) {
		this.mdResolverLookup = mdResolverLookup;
	}
	
	@Override
	public Maybe<? extends AuthorizedRequest> processReasoned(ServiceRequestContext requestContext, AuthorizedRequest request) {
		StatefulServiceAuthorization serviceAuthorization = new StatefulServiceAuthorization(requestContext, request);
		return serviceAuthorization.process();
	}
	
	private class StatefulServiceAuthorization {
		private ServiceRequestContext context;
		private Set<String> effectiveRoles;
		private AuthorizedRequest request;
		
		public StatefulServiceAuthorization(ServiceRequestContext context, AuthorizedRequest request) {
			this.context = context;
			this.request = request;
		}
		
		private Set<String> getEffectiveRoles() {
			if (effectiveRoles == null) {
				effectiveRoles = context.getAttribute(UserSessionAspect.class).getEffectiveRoles();
			}

			return effectiveRoles;
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
		
		private Maybe<? extends AuthorizedRequest> createForbiddenMaybe() {
			return Reasons.build(Forbidden.T).text("Insufficient priviledges to execute this request.").toMaybe();
		}
		
		public Maybe<? extends AuthorizedRequest> process() {
			String domainId = context.getDomainId();
			CmdResolver mdResolver = mdResolverLookup.apply(domainId);
			
			EntityMdResolver entityMdResolver = mdResolver.getMetaData().entity(request);
			GrantedFor grantedFor = entityMdResolver.meta(GrantedFor.T).exclusive();
			DeniedFor deniedFor = entityMdResolver.meta(DeniedFor.T).exclusive();
			
			if (grantedFor != null) {
				Set<String> roles = grantedFor.getRoles();
				if (!roles.isEmpty()) {
					if (!hasAnyRole(roles))
						return createForbiddenMaybe();
				}
			}
			
			if (deniedFor != null) {
				if (hasAnyRole(deniedFor.getRoles()))
					return createForbiddenMaybe();
			}
			
			return Maybe.complete(request);
		}
	}
}