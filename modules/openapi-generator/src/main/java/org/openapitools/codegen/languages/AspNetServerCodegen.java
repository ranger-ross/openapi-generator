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

package org.openapitools.codegen.languages;

import com.samskivert.mustache.Mustache;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import lombok.Setter;
import org.openapitools.codegen.*;
import org.openapitools.codegen.meta.features.*;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.openapitools.codegen.utils.ModelUtils;
import org.openapitools.codegen.utils.URLPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

import static java.util.UUID.randomUUID;

public class AspNetServerCodegen extends AbstractCSharpCodegen {

    public static final String USE_SWASHBUCKLE = "useSwashbuckle";
    public static final String MODEL_POCOMODE = "pocoModels";
    public static final String USE_MODEL_SEPERATEPROJECT = "useSeparateModelProject";
    public static final String ASPNET_CORE_VERSION = "aspnetCoreVersion";
    public static final String SWASHBUCKLE_VERSION = "swashbuckleVersion";
    public static final String CLASS_MODIFIER = "classModifier";
    public static final String OPERATION_MODIFIER = "operationModifier";
    public static final String OPERATION_IS_ASYNC = "operationIsAsync";
    public static final String OPERATION_RESULT_TASK = "operationResultTask";
    public static final String GENERATE_BODY = "generateBody";
    public static final String BUILD_TARGET = "buildTarget";
    public static final String MODEL_CLASS_MODIFIER = "modelClassModifier";
    public static final String TARGET_FRAMEWORK = "targetFramework";
    public static final String NET_60_OR_LATER = "net60OrLater";

    public static final String PROJECT_SDK = "projectSdk";
    public static final String SDK_WEB = "Microsoft.NET.Sdk.Web";
    public static final String SDK_LIB = "Microsoft.NET.Sdk";
    public static final String COMPATIBILITY_VERSION = "compatibilityVersion";
    public static final String IS_LIBRARY = "isLibrary";
    public static final String USE_FRAMEWORK_REFERENCE = "useFrameworkReference";
    public static final String USE_NEWTONSOFT = "useNewtonsoft";
    public static final String USE_DEFAULT_ROUTING = "useDefaultRouting";
    public static final String NEWTONSOFT_VERSION = "newtonsoftVersion";
    public static final String CENTRALIZED_PACKAGE_VERSION_MANAGEMENT = "centralizedPackageVersionManagement";
    public static final String USE_PACKAGE_VERSIONS = "usePackageVersions";
    public static final String DEFAULT = "default";
    public static final String ENABLE = "enable";
    public static final String OPTOUT = "optout";

    @Setter
    private String packageGuid = "{" + randomUUID().toString().toUpperCase(Locale.ROOT) + "}";
    private String userSecretsGuid = randomUUID().toString();

    protected final Logger LOGGER = LoggerFactory.getLogger(AspNetServerCodegen.class);

    private boolean useSwashbuckle = true;
    private boolean pocoModels = false;
    private boolean useSeparateModelProject = false;
    protected int serverPort = 8080;
    protected String serverHost = "0.0.0.0";
    protected CliOption swashbuckleVersion = new CliOption(SWASHBUCKLE_VERSION, "Swashbuckle version: 3.0.0 (deprecated), 4.0.0 (deprecated), 5.0.0 (deprecated), 6.4.0");
    protected CliOption aspnetCoreVersion = new CliOption(ASPNET_CORE_VERSION, "ASP.NET Core version: 6.0, 5.0, 3.1, 3.0, 2.2, 2.1, 2.0 (deprecated)");
    private CliOption classModifier = new CliOption(CLASS_MODIFIER, "Class Modifier for controller classes: Empty string or abstract.");
    private CliOption operationModifier = new CliOption(OPERATION_MODIFIER, "Operation Modifier can be virtual or abstract");
    private CliOption modelClassModifier = new CliOption(MODEL_CLASS_MODIFIER, "Model Class Modifier can be nothing or partial");
    private boolean generateBody = true;
    private CliOption buildTarget = new CliOption("buildTarget", "Target to build an application or library");
    private String projectSdk = SDK_WEB;
    private String compatibilityVersion = "Version_2_2";
    private boolean operationIsAsync = false;
    private boolean operationResultTask = false;
    private boolean isLibrary = false;
    private boolean useFrameworkReference = false;
    private boolean useNewtonsoft = true;
    private boolean useDefaultRouting = true;
    private String newtonsoftVersion = "3.0.0";
    private CliOption centralizedPackageVersionManagement = new CliOption(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT, "Option to control the usage of centralized package version management. https://devblogs.microsoft.com/nuget/introducing-central-package-management/#disabling-central-package-management");

    public AspNetServerCodegen() {
        super();

        // TODO: AspnetCore community review
        modifyFeatureSet(features -> features
                .includeDocumentationFeatures(DocumentationFeature.Readme)
                .excludeWireFormatFeatures(WireFormatFeature.PROTOBUF)
                .securityFeatures(EnumSet.of(
                        SecurityFeature.ApiKey,
                        SecurityFeature.BasicAuth,
                        SecurityFeature.BearerToken
                ))
                .excludeGlobalFeatures(
                        GlobalFeature.XMLStructureDefinitions,
                        GlobalFeature.Callbacks,
                        GlobalFeature.LinkObjects,
                        GlobalFeature.ParameterStyling,
                        GlobalFeature.MultiServer
                )
                .includeSchemaSupportFeatures(
                        SchemaSupportFeature.Polymorphism
                )
                .includeParameterFeatures(
                        ParameterFeature.Cookie
                )
        );

        outputFolder = "generated-code" + File.separator + getName();

        modelTemplateFiles.put("model.mustache", ".cs");
        apiTemplateFiles.put("controller.mustache", ".cs");

        embeddedTemplateDir = templateDir = "aspnetcore/3.0";

        // contextually reserved words
        // NOTE: C# uses camel cased reserved words, while models are title cased. We don't want lowercase comparisons.
        reservedWords.addAll(
                Arrays.asList("var", "async", "await", "dynamic", "yield")
        );

        cliOptions.clear();

        setSupportNullable(Boolean.TRUE);

        // CLI options
        addOption(CodegenConstants.PACKAGE_DESCRIPTION,
                CodegenConstants.PACKAGE_DESCRIPTION_DESC,
                packageDescription);

        addOption(CodegenConstants.LICENSE_URL,
                CodegenConstants.LICENSE_URL_DESC,
                licenseUrl);

        addOption(CodegenConstants.LICENSE_NAME,
                CodegenConstants.LICENSE_NAME_DESC,
                licenseName);

        addOption(CodegenConstants.PACKAGE_COPYRIGHT,
                CodegenConstants.PACKAGE_COPYRIGHT_DESC,
                packageCopyright);

        addOption(CodegenConstants.PACKAGE_AUTHORS,
                CodegenConstants.PACKAGE_AUTHORS_DESC,
                packageAuthors);

        addOption(CodegenConstants.PACKAGE_TITLE,
                CodegenConstants.PACKAGE_TITLE_DESC,
                packageTitle);

        addOption(CodegenConstants.PACKAGE_NAME,
                "C# package name (convention: Title.Case).",
                packageName);

        addOption(CodegenConstants.PACKAGE_VERSION,
                "C# package version.",
                packageVersion);

        addOption(CodegenConstants.OPTIONAL_PROJECT_GUID,
                CodegenConstants.OPTIONAL_PROJECT_GUID_DESC,
                null);

        addOption(CodegenConstants.SOURCE_FOLDER,
                CodegenConstants.SOURCE_FOLDER_DESC,
                sourceFolder);

        addOption(COMPATIBILITY_VERSION, "ASP.Net Core CompatibilityVersion", compatibilityVersion);

        aspnetCoreVersion.addEnum("2.0", "ASP.NET Core 2.0");
        aspnetCoreVersion.addEnum("2.1", "ASP.NET Core 2.1");
        aspnetCoreVersion.addEnum("2.2", "ASP.NET Core 2.2");
        aspnetCoreVersion.addEnum("3.0", "ASP.NET Core 3.0");
        aspnetCoreVersion.addEnum("3.1", "ASP.NET Core 3.1");
        aspnetCoreVersion.addEnum("5.0", "ASP.NET Core 5.0");
        aspnetCoreVersion.addEnum("6.0", "ASP.NET Core 6.0");
        aspnetCoreVersion.addEnum("7.0", "ASP.NET Core 7.0");
        aspnetCoreVersion.addEnum("8.0", "ASP.NET Core 8.0");
        aspnetCoreVersion.setDefault("8.0");
        aspnetCoreVersion.setOptValue(aspnetCoreVersion.getDefault());
        cliOptions.add(aspnetCoreVersion);

        swashbuckleVersion.addEnum("3.0.0", "Swashbuckle 3.0.0");
        swashbuckleVersion.addEnum("4.0.0", "Swashbuckle 4.0.0");
        swashbuckleVersion.addEnum("5.0.0", "Swashbuckle 5.0.0");
        swashbuckleVersion.addEnum("6.4.0", "Swashbuckle 6.4.0");
        swashbuckleVersion.setDefault("6.4.0");
        swashbuckleVersion.setOptValue(swashbuckleVersion.getDefault());
        cliOptions.add(swashbuckleVersion);

        // CLI Switches
        addSwitch(CodegenConstants.NULLABLE_REFERENCE_TYPES,
                CodegenConstants.NULLABLE_REFERENCE_TYPES_DESC,
                this.nullReferenceTypesFlag);

        addSwitch(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG,
                CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG_DESC,
                sortParamsByRequiredFlag);

        addSwitch(CodegenConstants.USE_DATETIME_OFFSET,
                CodegenConstants.USE_DATETIME_OFFSET_DESC,
                useDateTimeOffsetFlag);

        addSwitch(CodegenConstants.USE_DATETIME_FOR_DATE,
                CodegenConstants.USE_DATETIME_FOR_DATE_DESC,
                useDateTimeForDateFlag);

        addSwitch(CodegenConstants.USE_COLLECTION,
                CodegenConstants.USE_COLLECTION_DESC,
                useCollection);

        addSwitch(CodegenConstants.RETURN_ICOLLECTION,
                CodegenConstants.RETURN_ICOLLECTION_DESC,
                returnICollection);

        addSwitch(USE_SWASHBUCKLE,
                "Uses the Swashbuckle.AspNetCore NuGet package for documentation.",
                useSwashbuckle);

        addSwitch(MODEL_POCOMODE,
                "Build POCO Models",
                pocoModels);

        addSwitch(USE_MODEL_SEPERATEPROJECT,
                "Create a separate project for models",
                useSeparateModelProject);

        addSwitch(IS_LIBRARY,
                "Is the build a library",
                isLibrary);

        addSwitch(USE_FRAMEWORK_REFERENCE,
                "Use frameworkReference for ASP.NET Core 3.0+ and PackageReference ASP.NET Core 2.2 or earlier.",
                useFrameworkReference);

        addSwitch(USE_NEWTONSOFT,
                "Uses the Newtonsoft JSON library.",
                useNewtonsoft);


        addOption(NEWTONSOFT_VERSION,
                "Version for Microsoft.AspNetCore.Mvc.NewtonsoftJson for ASP.NET Core 3.0+",
                newtonsoftVersion);

        addSwitch(USE_DEFAULT_ROUTING,
                "Use default routing for the ASP.NET Core version.",
                useDefaultRouting);

        addOption(CodegenConstants.ENUM_NAME_SUFFIX,
                CodegenConstants.ENUM_NAME_SUFFIX_DESC,
                enumNameSuffix);

        addOption(CodegenConstants.ENUM_VALUE_SUFFIX,
                "Suffix that will be appended to all enum values.",
                enumValueSuffix);

        classModifier.addEnum("", "Keep class default with no modifier");
        classModifier.addEnum("abstract", "Make class abstract");
        classModifier.setDefault("");
        classModifier.setOptValue(classModifier.getDefault());
        addOption(classModifier.getOpt(), classModifier.getDescription(), classModifier.getOptValue());

        operationModifier.addEnum("virtual", "Keep method virtual");
        operationModifier.addEnum("abstract", "Make method abstract");
        operationModifier.setDefault("virtual");
        operationModifier.setOptValue(operationModifier.getDefault());
        cliOptions.add(operationModifier);

        buildTarget.addEnum("program", "Generate code for a standalone server");
        buildTarget.addEnum("library", "Generate code for a server abstract class library");
        buildTarget.setDefault("program");
        buildTarget.setOptValue(buildTarget.getDefault());
        cliOptions.add(buildTarget);

        addSwitch(GENERATE_BODY,
                "Generates method body.",
                generateBody);

        addSwitch(OPERATION_IS_ASYNC,
                "Set methods to async or sync (default).",
                operationIsAsync);

        addSwitch(OPERATION_RESULT_TASK,
                "Set methods result to Task<>.",
                operationResultTask);

        modelClassModifier.setType("String");
        modelClassModifier.addEnum("", "Keep model class default with no modifier");
        modelClassModifier.addEnum("partial", "Make model class partial");
        modelClassModifier.setDefault("partial");
        modelClassModifier.setOptValue(modelClassModifier.getDefault());
        addOption(modelClassModifier.getOpt(), modelClassModifier.getDescription(), modelClassModifier.getOptValue());

        addCentralizedPackageManagementOption();
    }

    private void addCentralizedPackageManagementOption() {
        Map<String, String> centralizedPackageVersionManagementOptions = new HashMap<>();
        centralizedPackageVersionManagementOptions.put(DEFAULT, "Property in project won't be used");
        centralizedPackageVersionManagementOptions.put(ENABLE, "Centralized package version management will be used");
        centralizedPackageVersionManagementOptions.put(OPTOUT, "Opt out of centralized package version management. Set this if you have a Directory.Packages.pros file but want this project to ignore it.");
        centralizedPackageVersionManagement.setEnum(centralizedPackageVersionManagementOptions);
        cliOptions.add(centralizedPackageVersionManagement);
    }

    @Deprecated
    @Override
    protected Set<String> getNullableTypes() {
        return new HashSet<>(Arrays.asList("decimal", "bool", "int", "uint", "long", "ulong", "float", "double",
                "DateTime", "DateOnly", "DateTimeOffset", "Guid"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public void postProcessParameter(CodegenParameter parameter) {
        super.postProcessParameter(parameter);

        if (!parameter.dataType.endsWith("?") && !parameter.required && (nullReferenceTypesFlag || this.getNullableTypes().contains(parameter.dataType))) {
            parameter.dataType = parameter.dataType + "?";
        }
    }

    @Override
    protected void updateCodegenParameterEnum(CodegenParameter parameter, CodegenModel model) {
        super.updateCodegenParameterEnumLegacy(parameter, model);

        if (!parameter.required && parameter.vendorExtensions.get("x-csharp-value-type") != null) { //optional
            parameter.dataType = parameter.dataType + "?";
        }
    }

    @Override
    public String getName() {
        return "aspnetcore";
    }

    @Override
    public String getHelp() {
        return "Generates an ASP.NET Core Web API server.";
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
        URL url = URLPathUtils.getServerURL(openAPI, serverVariableOverrides());
        additionalProperties.put("serverHost", url.getHost());
        additionalProperties.put("serverPort", URLPathUtils.getPort(url, 8080));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.OPTIONAL_PROJECT_GUID)) {
            setPackageGuid((String) additionalProperties.get(CodegenConstants.OPTIONAL_PROJECT_GUID));
        }
        additionalProperties.put("packageGuid", packageGuid);

        if (!additionalProperties.containsKey("packageGuid")) {
            additionalProperties.put("packageGuid", packageGuid);
        } else {
            packageGuid = (String) additionalProperties.get("packageGuid");
        }

        if (!additionalProperties.containsKey("userSecretsGuid")) {
            additionalProperties.put("userSecretsGuid", userSecretsGuid);
        } else {
            userSecretsGuid = (String) additionalProperties.get("userSecretsGuid");
        }

        if (!additionalProperties.containsKey(NEWTONSOFT_VERSION)) {
            additionalProperties.put(NEWTONSOFT_VERSION, newtonsoftVersion);
        } else {
            newtonsoftVersion = (String) additionalProperties.get(NEWTONSOFT_VERSION);
        }

        // Check for the modifiers etc.
        // The order of the checks is important.
        setBuildTarget();
        setClassModifier();
        setOperationModifier();
        setModelClassModifier();
        setPocoModels();
        setUseSeparateModelProject();
        setUseSwashbuckle();
        setOperationIsAsync();

        // Check for class modifier if not present set the default value.
        additionalProperties.put(PROJECT_SDK, projectSdk);

        additionalProperties.put("dockerTag", packageName.toLowerCase(Locale.ROOT));

        if (!additionalProperties.containsKey(CodegenConstants.API_PACKAGE)) {
            apiPackage = packageName + ".Controllers";
            additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        }

        if (!additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE)) {
            modelPackage = packageName + ".Models";
            additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        }

        String packageFolder = sourceFolder + File.separator + packageName;

        // determine the ASP.NET core version setting
        setAspnetCoreVersion(packageFolder);
        setSwashbuckleVersion();
        setIsFramework();
        setUseNewtonsoft();
        setUseEndpointRouting();

        supportingFiles.add(new SupportingFile("build.sh.mustache", "", "build.sh"));
        supportingFiles.add(new SupportingFile("build.bat.mustache", "", "build.bat"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("Solution.mustache", "", packageName + ".sln"));
        supportingFiles.add(new SupportingFile("gitignore", packageFolder, ".gitignore"));
        supportingFiles.add(new SupportingFile("validateModel.mustache", packageFolder + File.separator + "Attributes", "ValidateModelStateAttribute.cs"));

        if (useSeparateModelProject) {
            String separateModelSeparator = sourceFolder + File.separator + modelPackage;
            supportingFiles.add(new SupportingFile("gitignore", separateModelSeparator, ".gitignore"));
            supportingFiles.add(new SupportingFile("typeConverter.mustache", separateModelSeparator + File.separator + "Converters", "CustomEnumConverter.cs"));
            supportingFiles.add(new SupportingFile("ModelsProject.csproj.mustache", separateModelSeparator, modelPackage + ".csproj"));
        } else {
            supportingFiles.add(new SupportingFile("typeConverter.mustache", packageFolder + File.separator + "Converters", "CustomEnumConverter.cs"));
        }

        if (!aspnetCoreVersion.getOptValue().startsWith("2.")) {
            supportingFiles.add(new SupportingFile("OpenApi" + File.separator + "TypeExtensions.mustache", packageFolder + File.separator + "OpenApi", "TypeExtensions.cs"));
        }

        supportingFiles.add(new SupportingFile("Project.csproj.mustache", packageFolder, packageName + ".csproj"));
        if (!isLibrary) {
            supportingFiles.add(new SupportingFile("Dockerfile.mustache", packageFolder, "Dockerfile"));
            supportingFiles.add(new SupportingFile("appsettings.json", packageFolder, "appsettings.json"));
            supportingFiles.add(new SupportingFile("appsettings.Development.json", packageFolder, "appsettings.Development.json"));

            supportingFiles.add(new SupportingFile("Startup.mustache", packageFolder, "Startup.cs"));
            supportingFiles.add(new SupportingFile("Program.mustache", packageFolder, "Program.cs"));
            supportingFiles.add(new SupportingFile("Properties" + File.separator + "launchSettings.json",
                    packageFolder + File.separator + "Properties", "launchSettings.json"));
            // wwwroot files.
            supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "README.md", packageFolder + File.separator + "wwwroot", "README.md"));
            supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "index.html", packageFolder + File.separator + "wwwroot", "index.html"));
            supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "openapi-original.mustache",
                    packageFolder + File.separator + "wwwroot", "openapi-original.json"));
        } else {
            supportingFiles.add(new SupportingFile("Project.nuspec.mustache", packageFolder, packageName + ".nuspec"));
        }

        if (useSwashbuckle) {
            supportingFiles.add(new SupportingFile("Filters" + File.separator + "BasePathFilter.mustache",
                    packageFolder + File.separator + "Filters", "BasePathFilter.cs"));
            supportingFiles.add(new SupportingFile("Filters" + File.separator + "GeneratePathParamsValidationFilter.mustache",
                    packageFolder + File.separator + "Filters", "GeneratePathParamsValidationFilter.cs"));
        }

        supportingFiles.add(new SupportingFile("Authentication" + File.separator + "ApiAuthentication.mustache", packageFolder + File.separator + "Authentication", "ApiAuthentication.cs"));
        supportingFiles.add(new SupportingFile("Formatters" + File.separator + "InputFormatterStream.mustache", packageFolder + File.separator + "Formatters", "InputFormatterStream.cs"));

        this.setTypeMapping();


        setCentralizedPackageManagementOption();
    }

    private void setCentralizedPackageManagementOption() {
        additionalProperties.put(USE_PACKAGE_VERSIONS, true);

        if (additionalProperties.containsKey(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT)) {
            switch ((String) additionalProperties.get(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT)) {
                case DEFAULT:
                    additionalProperties.remove(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT);
                    break;
                case ENABLE:
                    additionalProperties.replace(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT, "true");
                    additionalProperties.put(USE_PACKAGE_VERSIONS, false);
                    break;
                case OPTOUT:
                    additionalProperties.replace(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT, "false");
                    break;
                default:
                    throw new RuntimeException("Invalid value `" + additionalProperties.get(CENTRALIZED_PACKAGE_VERSION_MANAGEMENT) + "` for the option `centralizedPackageVersionManagement`. Please refer to the documentation for more information.");
            }
        }
    }

    @Override
    protected boolean useNet60OrLater() {
        return additionalProperties.containsKey(NET_60_OR_LATER);
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + packageName + File.separator + "Controllers";
    }

    @Override
    public String modelFileFolder() {
        if (!useSeparateModelProject) {
            return outputFolder + File.separator + sourceFolder + File.separator + packageName + File.separator + "Models";
        } else {
            return outputFolder + File.separator + sourceFolder + File.separator + modelPackage;
        }
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        generateJSONSpecFile(objs);
        return super.postProcessSupportingFileData(objs);
    }

    @Override
    protected void processOperation(CodegenOperation operation) {
        super.processOperation(operation);

        // HACK: Unlikely in the wild, but we need to clean operation paths for MVC Routing
        if (operation.path != null) {
            String original = operation.path;
            operation.path = operation.path.replace("?", "/");
            if (!original.equals(operation.path)) {
                LOGGER.warn("Normalized {} to {}. Please verify generated source.", original, operation.path);
            }
        }

        // Converts, for example, PUT to HttpPut for controller attributes
        operation.httpMethod = "Http" + operation.httpMethod.charAt(0) + operation.httpMethod.substring(1).toLowerCase(Locale.ROOT);
    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        super.postProcessOperationsWithModels(objs, allModels);
        // We need to postprocess the operations to add proper consumes tags and fix form file handling
        if (objs != null) {
            OperationMap operations = objs.getOperations();
            if (operations != null) {
                List<CodegenOperation> ops = operations.getOperation();
                for (CodegenOperation operation : ops) {
                    if (operation.consumes == null) {
                        continue;
                    }
                    if (operation.consumes.size() == 0) {
                        continue;
                    }

                    // Build a consumes string for the operation we cannot iterate in the template as we need a ','
                    // after each entry but the last

                    StringBuilder consumesString = new StringBuilder();
                    for (Map<String, String> consume : operation.consumes) {
                        if (!consume.containsKey("mediaType")) {
                            continue;
                        }

                        if (consumesString.toString().isEmpty()) {
                            consumesString = new StringBuilder("\"" + consume.get("mediaType") + "\"");
                        } else {
                            consumesString.append(", \"").append(consume.get("mediaType")).append("\"");
                        }

                        // In a multipart/form-data consuming context binary data is best handled by an IFormFile
                        if (!consume.get("mediaType").equals("multipart/form-data")) {
                            continue;
                        }

                        // Change dataType of binary parameters to IFormFile for formParams in multipart/form-data
                        for (CodegenParameter param : operation.formParams) {
                            if (param.isBinary) {
                                param.dataType = "IFormFile";
                                param.baseType = "IFormFile";
                            }
                        }

                        for (CodegenParameter param : operation.allParams) {
                            if (param.isBinary && param.isFormParam) {
                                param.dataType = "IFormFile";
                                param.baseType = "IFormFile";
                            }
                        }
                    }

                    if (!consumesString.toString().isEmpty()) {
                        operation.vendorExtensions.put("x-aspnetcore-consumes", consumesString.toString());
                    }
                }
            }
        }
        return objs;
    }

    @Override
    public Mustache.Compiler processCompiler(Mustache.Compiler compiler) {
        // To avoid unexpected behaviors when options are passed programmatically such as { "useCollection": "" }
        return super.processCompiler(compiler).emptyStringIsFalse(true);
    }

    @Override
    public String toRegularExpression(String pattern) {
        return escapeText(pattern);
    }

    @Override
    protected void patchProperty(Map<String, CodegenModel> enumRefs, CodegenModel model, CodegenProperty property) {
        super.patchProperty(enumRefs, model, property);

        if (!property.isContainer && (this.getNullableTypes().contains(property.dataType) || property.isEnum)) {
            property.vendorExtensions.put("x-csharp-value-type", true);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getNullableType(Schema p, String type) {
        if (languageSpecificPrimitives.contains(type)) {
            if (isSupportNullable() && ModelUtils.isNullable(p) && (this.getNullableTypes().contains(type) || nullReferenceTypesFlag)) {
                return type + "?";
            } else {
                return type;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void patchVendorExtensionNullableValueType(CodegenParameter parameter) {
        super.patchVendorExtensionNullableValueTypeLegacy(parameter);
    }

    private void setCliOption(CliOption cliOption) throws IllegalArgumentException {
        if (additionalProperties.containsKey(cliOption.getOpt())) {
            // TODO Hack - not sure why the empty strings become boolean.
            Object obj = additionalProperties.get(cliOption.getOpt());
            if (!SchemaTypeUtil.BOOLEAN_TYPE.equals(cliOption.getType())) {
                if (obj instanceof Boolean) {
                    obj = "";
                    additionalProperties.put(cliOption.getOpt(), obj);
                }
            }
            cliOption.setOptValue(obj.toString());
        } else {
            additionalProperties.put(cliOption.getOpt(), cliOption.getOptValue());
        }
        if (cliOption.getOptValue() == null) {
            cliOption.setOptValue(cliOption.getDefault());
            throw new IllegalArgumentException(cliOption.getOpt() + ": Invalid value '" + additionalProperties.get(cliOption.getOpt()).toString() + "'" +
                    ". " + cliOption.getDescription());
        }
    }

    private void setClassModifier() {
        // CHeck for class modifier if not present set the default value.
        setCliOption(classModifier);

        // If class modifier is abstract then the methods need to be abstract too.
        if ("abstract".equals(classModifier.getOptValue())) {
            operationModifier.setOptValue(classModifier.getOptValue());
            additionalProperties.put(OPERATION_MODIFIER, operationModifier.getOptValue());
            LOGGER.warn("classModifier is {} so forcing operationModifier to {}", classModifier.getOptValue(), operationModifier.getOptValue());
        }
    }

    private void setOperationModifier() {
        setCliOption(operationModifier);

        // If operation modifier is abstract then dont generate any body
        if ("abstract".equals(operationModifier.getOptValue())) {
            generateBody = false;
            additionalProperties.put(GENERATE_BODY, generateBody);
            LOGGER.warn("operationModifier is {} so forcing generateBody to {}", operationModifier.getOptValue(), generateBody);
        } else if (additionalProperties.containsKey(GENERATE_BODY)) {
            generateBody = convertPropertyToBooleanAndWriteBack(GENERATE_BODY);
        } else {
            additionalProperties.put(GENERATE_BODY, generateBody);
        }
    }

    private void setModelClassModifier() {
        setCliOption(modelClassModifier);

        // If operation modifier is abstract then dont generate any body
        if (isLibrary) {
            modelClassModifier.setOptValue("");
            additionalProperties.put(MODEL_CLASS_MODIFIER, modelClassModifier.getOptValue());
            LOGGER.warn("buildTarget is {} so removing any modelClassModifier ", buildTarget.getOptValue());
        }
    }

    private void setBuildTarget() {
        setCliOption(buildTarget);
        if ("library".equals(buildTarget.getOptValue())) {
            LOGGER.warn("buildTarget is {} so changing default isLibrary to true", buildTarget.getOptValue());
            isLibrary = true;
            projectSdk = SDK_LIB;
            additionalProperties.put(CLASS_MODIFIER, "abstract");
        } else {
            isLibrary = false;
            projectSdk = SDK_WEB;
        }
        additionalProperties.put(IS_LIBRARY, isLibrary);
    }

    private void setAspnetCoreVersion(String packageFolder) {
        setCliOption(aspnetCoreVersion);

        if (!aspnetCoreVersion.getOptValue().startsWith("2.")) {
            compatibilityVersion = null;
        } else if ("2.0".equals(aspnetCoreVersion.getOptValue())) {
            compatibilityVersion = null;
        } else {
            // default, do nothing
            compatibilityVersion = "Version_" + aspnetCoreVersion.getOptValue().replace(".", "_");
        }
        LOGGER.info("ASP.NET core version: {}", aspnetCoreVersion.getOptValue());
        if (!additionalProperties.containsKey(CodegenConstants.TEMPLATE_DIR)) {
            templateDir = embeddedTemplateDir = "aspnetcore" + File.separator + determineTemplateVersion(aspnetCoreVersion.getOptValue());
        }
        additionalProperties.put(COMPATIBILITY_VERSION, compatibilityVersion);
    }

    private String determineTemplateVersion(String frameworkVersion) {
        switch (frameworkVersion) {
            case "8.0":
            case "7.0":
            case "6.0":
            case "5.0":
            case "3.1":
                return "3.0";

            case "2.2":
                return "2.1";

            default:
                return frameworkVersion;
        }
    }

    private void setPocoModels() {
        if (additionalProperties.containsKey(MODEL_POCOMODE)) {
            pocoModels = convertPropertyToBooleanAndWriteBack(MODEL_POCOMODE);
        } else {
            additionalProperties.put(MODEL_POCOMODE, pocoModels);
        }
    }

    private void setUseSeparateModelProject() {
        if (additionalProperties.containsKey(USE_MODEL_SEPERATEPROJECT)) {
            useSeparateModelProject = convertPropertyToBooleanAndWriteBack(USE_MODEL_SEPERATEPROJECT);
            if (useSeparateModelProject) {
                LOGGER.info("Using separate model project");
            }
        } else {
            additionalProperties.put(USE_MODEL_SEPERATEPROJECT, useSeparateModelProject);
        }
    }

    private void setUseSwashbuckle() {
        if (isLibrary) {
            LOGGER.warn("isLibrary is true so changing default useSwashbuckle to false");
            useSwashbuckle = false;
        } else {
            useSwashbuckle = true;
        }
        if (additionalProperties.containsKey(USE_SWASHBUCKLE)) {
            useSwashbuckle = convertPropertyToBooleanAndWriteBack(USE_SWASHBUCKLE);
        } else {
            additionalProperties.put(USE_SWASHBUCKLE, useSwashbuckle);
        }
    }

    private void setOperationIsAsync() {
        if (isLibrary) {
            operationIsAsync = false;
            additionalProperties.put(OPERATION_IS_ASYNC, operationIsAsync);
        } else if (additionalProperties.containsKey(OPERATION_IS_ASYNC)) {
            operationIsAsync = convertPropertyToBooleanAndWriteBack(OPERATION_IS_ASYNC);
        } else {
            additionalProperties.put(OPERATION_IS_ASYNC, operationIsAsync);
        }
    }

    private void setIsFramework() {
        if (aspnetCoreVersion.getOptValue().startsWith("3.")) {// default, do nothing
            LOGGER.warn(
                    "ASP.NET core version is {} so changing to use frameworkReference instead of packageReference ",
                    aspnetCoreVersion.getOptValue());
            useFrameworkReference = true;
            additionalProperties.put(USE_FRAMEWORK_REFERENCE, useFrameworkReference);
            additionalProperties.put(TARGET_FRAMEWORK, "netcoreapp" + aspnetCoreVersion.getOptValue());
        } else if (aspnetCoreVersion.getOptValue().startsWith("5.")) {// default, do nothing
            LOGGER.warn(
                    "ASP.NET core version is {} so changing to use frameworkReference instead of packageReference ",
                    aspnetCoreVersion.getOptValue());
            useFrameworkReference = true;
            additionalProperties.put(USE_FRAMEWORK_REFERENCE, useFrameworkReference);
            additionalProperties.put(TARGET_FRAMEWORK, "net5.0");
        } else if (aspnetCoreVersion.getOptValue().startsWith("6.")) {
            LOGGER.warn(
                    "ASP.NET core version is {} so changing to use frameworkReference instead of packageReference ",
                    aspnetCoreVersion.getOptValue());
            useFrameworkReference = true;
            additionalProperties.put(USE_FRAMEWORK_REFERENCE, useFrameworkReference);
            additionalProperties.put(TARGET_FRAMEWORK, "net6.0");
        } else if (aspnetCoreVersion.getOptValue().startsWith("7.")) {
            LOGGER.warn(
                    "ASP.NET core version is {} so changing to use frameworkReference instead of packageReference ",
                    aspnetCoreVersion.getOptValue());
            useFrameworkReference = true;
            additionalProperties.put(USE_FRAMEWORK_REFERENCE, useFrameworkReference);
            additionalProperties.put(TARGET_FRAMEWORK, "net7.0");
        } else if (aspnetCoreVersion.getOptValue().startsWith("8.")) {
            LOGGER.warn(
                    "ASP.NET core version is {} so changing to use frameworkReference instead of packageReference ",
                    aspnetCoreVersion.getOptValue());
            useFrameworkReference = true;
            additionalProperties.put(USE_FRAMEWORK_REFERENCE, useFrameworkReference);
            additionalProperties.put(TARGET_FRAMEWORK, "net8.0");
        } else {
            if (additionalProperties.containsKey(USE_FRAMEWORK_REFERENCE)) {
                useFrameworkReference = convertPropertyToBooleanAndWriteBack(USE_FRAMEWORK_REFERENCE);
            } else {
                additionalProperties.put(USE_FRAMEWORK_REFERENCE, useFrameworkReference);
            }
            additionalProperties.put(TARGET_FRAMEWORK, "netcoreapp" + aspnetCoreVersion);
        }

        setAdditionalPropertyForFramework();
    }

    private void setAdditionalPropertyForFramework() {
        String targetFramework = ((String) additionalProperties.get(TARGET_FRAMEWORK));
        if (targetFramework.startsWith("net6.0") ||
                targetFramework.startsWith("net7.0") ||
                targetFramework.startsWith("net8.0")) {
            additionalProperties.put(NET_60_OR_LATER, true);
        }
    }

    private void setUseNewtonsoft() {
        if (aspnetCoreVersion.getOptValue().startsWith("2.")) {
            LOGGER.warn("ASP.NET core version 2.X support has been deprecated. Please use ASP.NET core version 3.1 instead");
            LOGGER.warn("ASP.NET core version is {} so staying on default json library.", aspnetCoreVersion.getOptValue());
            useNewtonsoft = false;
            additionalProperties.put(USE_NEWTONSOFT, useNewtonsoft);
        } else {
            if (additionalProperties.containsKey(USE_NEWTONSOFT)) {
                useNewtonsoft = convertPropertyToBooleanAndWriteBack(USE_NEWTONSOFT);
            } else {
                additionalProperties.put(USE_NEWTONSOFT, useNewtonsoft);
            }
        }
    }

    private void setUseEndpointRouting() {
        if (aspnetCoreVersion.getOptValue().startsWith("3.") || aspnetCoreVersion.getOptValue().startsWith("5.") || aspnetCoreVersion.getOptValue().startsWith("6.")) {
            LOGGER.warn("ASP.NET core version is {} so switching to old style endpoint routing.", aspnetCoreVersion.getOptValue());
            useDefaultRouting = false;
            additionalProperties.put(USE_DEFAULT_ROUTING, useDefaultRouting);
        } else {
            if (additionalProperties.containsKey(USE_DEFAULT_ROUTING)) {
                useDefaultRouting = convertPropertyToBooleanAndWriteBack(USE_DEFAULT_ROUTING);
            } else {
                additionalProperties.put(USE_DEFAULT_ROUTING, useDefaultRouting);
            }
        }
    }

    private void setSwashbuckleVersion() {
        setCliOption(swashbuckleVersion);

        if (aspnetCoreVersion.getOptValue().startsWith("3.")) {
            LOGGER.warn("ASP.NET core version is {} so changing default Swashbuckle version to 6.4.0.", aspnetCoreVersion.getOptValue());
            swashbuckleVersion.setOptValue("6.4.0");
            additionalProperties.put(SWASHBUCKLE_VERSION, swashbuckleVersion.getOptValue());
        } else if (aspnetCoreVersion.getOptValue().startsWith("5.")) {
            // for aspnet core 5.x, use Swashbuckle 6.4 instead
            LOGGER.warn("ASP.NET core version is {} so changing default Swashbuckle version to 6.4.0.", aspnetCoreVersion.getOptValue());
            swashbuckleVersion.setOptValue("6.4.0");
            additionalProperties.put(SWASHBUCKLE_VERSION, swashbuckleVersion.getOptValue());
        } else if (aspnetCoreVersion.getOptValue().startsWith("6.")) {
            LOGGER.warn("ASP.NET core version is {} so changing default Swashbuckle version to 6.4.0.", aspnetCoreVersion.getOptValue());
            swashbuckleVersion.setOptValue("6.4.0");
            additionalProperties.put(SWASHBUCKLE_VERSION, swashbuckleVersion.getOptValue());
        } else {
            // default, do nothing
            LOGGER.info("Swashbuckle version: {}", swashbuckleVersion.getOptValue());
        }
    }
}
