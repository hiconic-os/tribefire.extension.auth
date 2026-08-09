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
package tribefire.extension.auth.test;

import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.model.processing.service.api.SessionIdAspect;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;

import hiconic.rx.test.common.AbstractRxTest;
import tribefire.extension.auth.rbac.model.api.GetCurrentUserRequestAuthorizations;
import tribefire.extension.auth.rbac.model.api.GetRbacRequestAuthorizations;
import tribefire.extension.auth.rbac.model.api.data.CurrentUserRequestAuthorizations;
import tribefire.extension.auth.rbac.model.api.data.RbacRequestAuthorization;
import tribefire.extension.auth.rbac.model.api.data.RbacRequestAuthorizations;
import tribefire.extension.auth.test.model.ProcessWithRole1;
import tribefire.extension.auth.test.model.ProcessWithRole1AndWithoutRole2;
import tribefire.extension.auth.test.model.ProcessWithRole2;
import tribefire.extension.auth.test.model.ProcessWithRoleOverride;
import tribefire.extension.auth.test.model.ProcessWithInducedRole;
import tribefire.extension.auth.test.model.ProcessWithoutRole;

public class RbacTest extends AbstractRxTest {

	@BeforeClass
	public static void onBeforeClass() {
	}

	@Test
	public void testReflection() {
		GetCurrentUserRequestAuthorizations currentUserRequest = GetCurrentUserRequestAuthorizations.T.create();
		CurrentUserRequestAuthorizations currentUserAuthorizations = runAuthenticated("guest",
				() -> currentUserRequest.eval(evaluator).getReasoned()).get();
		Assertions.assertThat(currentUserAuthorizations.getAuthorizations()).isNotEmpty();

		GetRbacRequestAuthorizations rbacRequest = GetRbacRequestAuthorizations.T.create();
		RbacRequestAuthorizations rbacAuthorizations = runAuthenticated("admin",
				() -> rbacRequest.eval(evaluator).getReasoned()).get();

		RbacRequestAuthorization overridden = authorizationFor(rbacAuthorizations, ProcessWithRoleOverride.T.getTypeSignature());
		Assertions.assertThat(overridden.getOverrideRoles()).contains("internal", "role2");

		RbacRequestAuthorization inducing = authorizationFor(rbacAuthorizations, ProcessWithInducedRole.T.getTypeSignature());
		Assertions.assertThat(inducing.getInducedRoles()).containsExactly("role2");
	}

	private static RbacRequestAuthorization authorizationFor(RbacRequestAuthorizations authorizations, String typeSignature) {
		return authorizations.getAuthorizations().stream()
				.filter(a -> typeSignature.equals(a.getType()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing authorization reflection for " + typeSignature));
	}
	
	@Test
	public void testRoleFreeExecutionAllowed() {
		ProcessWithoutRole request = ProcessWithoutRole.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("guest", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isSatisfied()).isTrue();
	}
	
	@Test
	public void testRoleFreeExecutionForbidden() {
		ProcessWithRole1 request = ProcessWithRole1.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("guest", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isUnsatisfiedBy(Forbidden.T)).isTrue();
	}
	
	@Test
	public void testRole1ExecutionAllowed() {
		ProcessWithRole1 request = ProcessWithRole1.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("user1", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isSatisfied()).isTrue();
	}
	
	@Test
	public void testRole1ExecutionForbidden() {
		ProcessWithRole2 request = ProcessWithRole2.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("user1", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isUnsatisfiedBy(Forbidden.T)).isTrue();
	}
	
	@Test
	public void testRole1ExecutionForbiddenByDenied() {
		ProcessWithRole1AndWithoutRole2 request = ProcessWithRole1AndWithoutRole2.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("user3", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isUnsatisfiedBy(Forbidden.T)).isTrue();
	}
	
	@Test
	public void testRole2ExecutionAllowed() {
		ProcessWithRole1AndWithoutRole2 request = ProcessWithRole1AndWithoutRole2.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("user1", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isSatisfied()).isTrue();
	}

	@Test
	public void testRequestOverrideRoleTakesPrecedenceOverDenyRole() {
		ProcessWithRoleOverride request = ProcessWithRoleOverride.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("user2", () -> request.eval(evaluator).getReasoned());

		Assertions.assertThat(reasoned.isSatisfied()).isTrue();
	}

	@Test
	public void testInducedRoleIsAvailableToNestedEvaluationOnly() {
		runAuthenticated("user1", () -> {
			Maybe<Neutral> directBefore = ProcessWithRole2.T.create().eval(evaluator).getReasoned();
			Assertions.assertThat(directBefore.isUnsatisfiedBy(Forbidden.T)).isTrue();

			Maybe<Neutral> nested = ProcessWithInducedRole.T.create().eval(evaluator).getReasoned();
			Assertions.assertThat(nested.isSatisfied()).isTrue();

			Maybe<Neutral> directAfter = ProcessWithRole2.T.create().eval(evaluator).getReasoned();
			Assertions.assertThat(directAfter.isUnsatisfiedBy(Forbidden.T)).isTrue();
			return null;
		});
	}

	@Test
	public void testInducedRoleCannotAuthorizeItsDeclaringRequest() {
		Maybe<Neutral> reasoned = runAuthenticated("guest", () -> ProcessWithInducedRole.T.create().eval(evaluator).getReasoned());

		Assertions.assertThat(reasoned.isUnsatisfiedBy(Forbidden.T)).isTrue();
	}
	
	private <T> T runAuthenticated(String user, Supplier<T> runner) {
		OpenUserSessionWithUserAndPassword openSession = OpenUserSessionWithUserAndPassword.T.create();
		openSession.setUser(user);
		openSession.setPassword("test");
		
		OpenUserSessionResponse response = openSession.eval(evaluator).get();
		UserSession userSession = response.getUserSession();
		
		String sessionId = userSession.getSessionId();
		
		try {
			return AttributeContexts.derivePeek() //
				.set(SessionIdAspect.class, sessionId).buildAnd().execute(runner);
		}
		finally {
			Logout logout = Logout.T.create();
			logout.setSessionId(response.getUserSession().getSessionId());
			logout.eval(evaluator).get();
		}
	}

}
