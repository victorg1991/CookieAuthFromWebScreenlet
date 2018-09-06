package victor.com.cookieauthfromwebscreenlet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import com.liferay.mobile.android.auth.basic.CookieAuthentication;
import com.liferay.mobile.android.callback.typed.JSONObjectCallback;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.service.SessionImpl;
import com.liferay.mobile.android.v7.user.UserService;
import com.liferay.mobile.screens.context.LiferayServerContext;
import com.liferay.mobile.screens.context.SessionContext;
import com.liferay.mobile.screens.util.LiferayLogger;
import com.liferay.mobile.screens.web.WebListener;
import com.liferay.mobile.screens.web.WebScreenlet;
import com.liferay.mobile.screens.web.WebScreenletConfiguration;
import com.liferay.mobile.screens.web.util.JsScript;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements WebListener {

	private WebScreenlet webScreenlet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		/* Avoid configuring login screenlet for the sake of simplicity*/
		SessionContext.createBasicSession("demo@liferay.com", "demo");

		LiferayServerContext.setServer("http://screens.liferay.org.es");
		/**/

		webScreenlet = findViewById(R.id.web_screenlet);

		WebScreenletConfiguration configuration = new WebScreenletConfiguration.Builder("/web/guest/home")
			.load();
		webScreenlet.setWebScreenletConfiguration(configuration);
		webScreenlet.load();

		webScreenlet.setListener(this);
	}

	@Override
	public void onPageLoaded(String url) {
		JsScript script = new JsScript("session", "window.Screens.postMessage('token', Liferay.authToken);");

		webScreenlet.injectScript(script);
	}

	@Override
	public void onScriptMessageHandler(String namespace, String body) {

		String cookieHeader = CookieManager.getInstance().getCookie("http://screens.liferay.org.es");

		String authToken = body;

		// Create a authentication from authToken and cookie. Do not store this authentication
		// because it will be invalid after the cookie expired.
		// And as we don't have username and password we cannot refresh it
		CookieAuthentication cookieAuthentication = new CookieAuthentication(authToken, cookieHeader, "", "", false, 0, 0);

		Session session = new SessionImpl("http://screens.liferay.org.es", cookieAuthentication);

		session.setCallback(new JSONObjectCallback() {
			@Override
			public void onFailure(Exception exception) {
				LiferayLogger.e(exception.getMessage());
			}

			@Override
			public void onSuccess(JSONObject result) {
				LiferayLogger.d(result.toString());
			}
		});

		try {
			new UserService(session).getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void error(Exception e, String userAction) {

	}
}
