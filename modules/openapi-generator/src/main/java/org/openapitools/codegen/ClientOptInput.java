/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import lombok.Getter;
import org.openapitools.codegen.api.TemplateDefinition;
import org.openapitools.codegen.auth.AuthParser;
import org.openapitools.codegen.config.GeneratorSettings;

import java.util.List;

public class ClientOptInput {
    private CodegenConfig config;
    private GeneratorSettings generatorSettings;
    private OpenAPI openAPI;
    private List<AuthorizationValue> auths;
    // not deprecated as this is added to match other functionality, we need to move to Context<?> instead of ClientOptInput.
    @Getter private List<TemplateDefinition> userDefinedTemplates;

    public ClientOptInput openAPI(OpenAPI openAPI) {
        this.setOpenAPI(openAPI);
        return this;
    }

    public ClientOptInput generatorSettings(GeneratorSettings generatorSettings) {
        this.setGeneratorSettings(generatorSettings);
        return this;
    }

    public ClientOptInput config(CodegenConfig codegenConfig) {
        this.setConfig(codegenConfig);
        return this;
    }

    public ClientOptInput userDefinedTemplates(List<TemplateDefinition> userDefinedTemplates) {
        this.userDefinedTemplates = userDefinedTemplates;
        return this;
    }

    @Deprecated
    public ClientOptInput auth(String urlEncodedAuthString) {
        this.setAuth(urlEncodedAuthString);
        return this;
    }

    @Deprecated
    public String getAuth() {
        return AuthParser.reconstruct(auths);
    }

    @Deprecated
    public void setAuth(String urlEncodedAuthString) {
        this.auths = AuthParser.parse(urlEncodedAuthString);
    }

    @Deprecated
    public List<AuthorizationValue> getAuthorizationValues() {
        return auths;
    }

    @Deprecated
    public CodegenConfig getConfig() {
        return config;
    }

    /**
     * Sets the generator/config instance
     *
     * @param config codegen config
     * @deprecated use {@link #config(CodegenConfig)} instead
     */
    @Deprecated
    public void setConfig(CodegenConfig config) {
        this.config = config;
        // TODO: ClientOptInputs needs to be retired
        if (this.openAPI != null) {
            this.config.setOpenAPI(this.openAPI);
        }
    }

    @Deprecated
    public GeneratorSettings getGeneratorSettings() {
        return generatorSettings;
    }

    @Deprecated
    private void setGeneratorSettings(GeneratorSettings generatorSettings) {
        this.generatorSettings = generatorSettings;
        // TODO: ClientOptInputs needs to be retired
        if (this.openAPI != null) {
            this.config.setOpenAPI(this.openAPI);
        }
    }

    @Deprecated
    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    /**
     * Sets the OpenAPI document
     *
     * @param openAPI the specification
     * @deprecated use {@link #openAPI(OpenAPI)} instead
     */
    @Deprecated
    public void setOpenAPI(OpenAPI openAPI) {
        this.openAPI = openAPI;
        // TODO: ClientOptInputs needs to be retired
        if (this.config != null) {
            this.config.setOpenAPI(this.openAPI);
        }
    }
}
