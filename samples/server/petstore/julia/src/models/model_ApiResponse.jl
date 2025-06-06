# This file was generated by the Julia OpenAPI Code Generator
# Do not modify this file directly. Modify the OpenAPI specification instead.


@doc raw"""ApiResponse
Describes the result of uploading an image resource

    ApiResponse(;
        code=nothing,
        type=nothing,
        message=nothing,
    )

    - code::Int64
    - type::String
    - message::String
"""
Base.@kwdef mutable struct ApiResponse <: OpenAPI.APIModel
    code::Union{Nothing, Int64} = nothing
    type::Union{Nothing, String} = nothing
    message::Union{Nothing, String} = nothing

    function ApiResponse(code, type, message, )
        o = new(code, type, message, )
        OpenAPI.validate_properties(o)
        return o
    end
end # type ApiResponse

const _property_types_ApiResponse = Dict{Symbol,String}(Symbol("code")=>"Int64", Symbol("type")=>"String", Symbol("message")=>"String", )
OpenAPI.property_type(::Type{ ApiResponse }, name::Symbol) = Union{Nothing,eval(Base.Meta.parse(_property_types_ApiResponse[name]))}

function OpenAPI.check_required(o::ApiResponse)
    true
end

function OpenAPI.validate_properties(o::ApiResponse)
    OpenAPI.validate_property(ApiResponse, Symbol("code"), o.code)
    OpenAPI.validate_property(ApiResponse, Symbol("type"), o.type)
    OpenAPI.validate_property(ApiResponse, Symbol("message"), o.message)
end

function OpenAPI.validate_property(::Type{ ApiResponse }, name::Symbol, val)

    if name === Symbol("code")
        OpenAPI.validate_param(name, "ApiResponse", :format, val, "int32")
    end


end
