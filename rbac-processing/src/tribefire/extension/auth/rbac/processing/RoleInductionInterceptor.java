// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.auth.rbac.processing;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.common.UserInfo;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ReasonedServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * Makes roles declared through {@code InduceRoles} effective while an already-authorized request is processed.
 *
 * <p>The interceptor deliberately runs around request processing, independently of the authorization preprocessor.
 * Consequently induced roles cannot authorize the request that declares them; they are visible only to nested work.
 * The persisted session is never modified.</p>
 */
public class RoleInductionInterceptor implements ReasonedServiceAroundProcessor<AuthorizedRequest, Object> {

	private ServiceAuthorizationContext authorizationContext;

	@Required
	public void setAuthorizationContext(ServiceAuthorizationContext authorizationContext) {
		this.authorizationContext = authorizationContext;
	}

	@Override
	public Maybe<? extends Object> processReasoned(ServiceRequestContext context, AuthorizedRequest request, ProceedContext proceedContext) {
		ServiceDomainAuthorizationResolver resolver = authorizationContext.getDomainResolver(context.getDomainId());
		Set<String> inducedRoles = resolver.resolveInducedRoles(request.entityType());
		if (inducedRoles.isEmpty())
			return proceedContext.proceedReasoned(request);

		UserSession originalSession = context.getAttribute(UserSessionAspect.class);
		UserSession scopedSession = withAdditionalRoles(originalSession, inducedRoles);
		ServiceRequestContext scopedContext = withSession(context, scopedSession);

		return AttributeContexts.derivePeek() // Keep legacy consumers of the ambient AttributeContext consistent.
				.set(UserSessionAspect.class, scopedSession)
				.set(UserInfoAttribute.class, userInfo(scopedSession))
				.buildAnd()
				.execute(() -> proceedContext.proceedReasoned(scopedContext, request));
	}

	private static ServiceRequestContext withSession(ServiceRequestContext context, UserSession session) {
		ServiceRequestContextBuilder builder = context.derive();
		builder.set(UserSessionAspect.class, session);
		builder.set(UserInfoAttribute.class, userInfo(session));
		return builder.build();
	}

	private static UserInfo userInfo(UserSession session) {
		return UserInfo.of(session.getUser().getName(), session.getEffectiveRoles());
	}

	private static UserSession withAdditionalRoles(UserSession source, Set<String> additionalRoles) {
		Set<String> roles = new HashSet<>(source.getEffectiveRoles());
		if (!roles.addAll(additionalRoles))
			return source;

		UserSession target = UserSession.T.create();
		target.setAccessId(source.getAccessId());
		target.setSessionId(source.getSessionId());
		target.setUser(source.getUser());
		target.setCreationDate(source.getCreationDate());
		target.setFixedExpiryDate(source.getFixedExpiryDate());
		target.setExpiryDate(source.getExpiryDate());
		target.setLastAccessedDate(source.getLastAccessedDate());
		target.setMaxIdleTime(source.getMaxIdleTime());
		target.setIsInvalidated(source.getIsInvalidated());
		target.setEffectiveRoles(roles);
		target.setReferenceCounter(source.getReferenceCounter());
		target.setType(source.getType());
		target.setCreationNodeId(source.getCreationNodeId());
		target.setCreationInternetAddress(source.getCreationInternetAddress());
		target.setProperties(source.getProperties());
		return target;
	}
}
