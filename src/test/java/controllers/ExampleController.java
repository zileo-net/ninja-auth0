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

/**
 * Copyright (C) 2013 the original author or authors.
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

package controllers;

import ninja.Result;
import ninja.Results;

import com.google.inject.Singleton;

import net.zileo.ninja.auth0.subject.Auth0;
import net.zileo.ninja.auth0.subject.Subject;
import net.zileo.ninja.auth0.utils.Auth0Subject;

/**
 * Dummy controller.
 * 
 * @author jlannoy
 */
@Singleton
public class ExampleController {

    public Result index() {
        return Results.html();
    }
    
    public Result helloPublic() {
        return Results.html();
    }

    public Result helloPrivate() {
        return Results.html();
    }

    public Result helloSubject(@Auth0 Auth0Subject subject) {
        return Results.html().render("Subject", subject).render("subjectClass", subject.getClass().getName());
    }
    
    public Result helloSubjectWrongFilter(@Auth0 Auth0Subject subject) {
        return Results.html().template("views/ExampleController/helloSubject.ftl.html").render("Subject", subject).render("subjectClass", subject.getClass().getName());
    }

    public Result helloPublicJson() {
        return Results.json().render(new SimplePojo("Hello Public!"));
    }

    public Result helloPrivateJson() {
        return Results.json().render(new SimplePojo("Hello Private!"));
    }

    public Result helloSubjectJson(@Auth0 Subject subject) {
        return Results.json().render(subject);
    }

    public static class SimplePojo {
        public String content;

        public SimplePojo(String content) {
            this.content = content;
        }
    }
}
