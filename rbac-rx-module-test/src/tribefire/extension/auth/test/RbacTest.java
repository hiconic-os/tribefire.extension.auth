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
import com.braintribe.model.processing.service.api.SessionIdAspect;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;

import hiconic.rx.test.common.AbstractRxTest;
import tribefire.extension.auth.test.model.TestRequest;

public class RbacTest extends AbstractRxTest {

	@BeforeClass
	public static void onBeforeClass() {
	}

	@Test
	public void testExecution() {
		TestRequest request = TestRequest.T.create();
		Maybe<Neutral> reasoned = runAuthenticated("admin", () -> request.eval(evaluator).getReasoned());
		
		Assertions.assertThat(reasoned.isSatisfied());
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
