/**
 * Copyright (C) 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package conf;

import com.google.inject.Inject;

import controllers.ExampleController;
import net.zileo.ninja.auth0.Auth0Routes;
import net.zileo.ninja.auth0.AuthenticateFilter;
import ninja.Router;
import ninja.application.ApplicationRoutes;

public class Routes implements ApplicationRoutes {

    @Inject
    Auth0Routes auth0Routes;
    
    @SuppressWarnings("unchecked")
    @Override
    public void init(Router router) {
        auth0Routes.init(router);

        router.GET().route("/").with(ExampleController::index);
        
        router.GET().route("/helloPublic").with(ExampleController::helloPublic);
        router.GET().route("/helloPrivate").filters(AuthenticateFilter.class).with(ExampleController::helloPrivate);
        router.GET().route("/helloSubject").filters(AuthenticateFilter.class).with(ExampleController::helloSubject);

        router.GET().route("/helloPublic.json").with(ExampleController::helloPublicJson);
        router.GET().route("/helloPrivate.json").filters(AuthenticateFilter.class).with(ExampleController::helloPrivateJson);
        router.GET().route("/helloSubject.json").filters(AuthenticateFilter.class).with(ExampleController::helloSubjectJson);
    }

}
