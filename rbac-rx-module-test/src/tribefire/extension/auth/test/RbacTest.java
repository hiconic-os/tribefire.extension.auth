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
import tribefire.extension.auth.test.model.ProcessWithRole1;
import tribefire.extension.auth.test.model.ProcessWithRole1AndWithoutRole2;
import tribefire.extension.auth.test.model.ProcessWithRole2;
import tribefire.extension.auth.test.model.ProcessWithoutRole;

public class RbacTest extends AbstractRxTest {

	@BeforeClass
	public static void onBeforeClass() {
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
