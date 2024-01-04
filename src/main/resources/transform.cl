const sampler_t samplerIn =
    CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST;

inline float2 skew2D(__read_only image2d_t image, float2 point, float2 skew) {
    float2 center = 0.5f * (float2)(get_image_width(image), get_image_height(image));
    float skewX = skew.x * (point.y - center.y);
    float skewY = skew.y * (point.x - center.x);
    float2 skewedPoint = (float2)(point.x + skewX, point.y + skewY);
    return skewedPoint;
}

inline float2 rotate2D(__read_only image2d_t image, float2 point, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float2 center = 0.5f * (float2)(get_image_width(image), get_image_height(image));
    float2 rotatedPoint = (float2)(c * (point.x - center.x) - s * (point.y - center.y),
                                  s * (point.x - center.x) + c * (point.y - center.y));
    return rotatedPoint + center;
}

inline float2 translate2D(__read_only image2d_t image, float2 point, float4 translation) {
    float tUp = translation.x * get_image_height(image);
    float tDown = translation.y * get_image_height(image);
    float tLeft = translation.z * get_image_width(image);
    float tRight = translation.w * get_image_width(image);
    float2 translatedPoint = (float2)(point.x + tLeft - tRight, point.y + tUp - tDown);
    return translatedPoint;
}

__kernel void main(__read_only image2d_t input, __write_only image2d_t output,
                   float4 translate,
                   float rotation,
                   float2 skew) {
    int2 gid = (int2)(get_global_id(0), get_global_id(1));
    float2 originalCoords = (float2)(gid.x, gid.y);
    float2 skewedCoords = skew2D(input, originalCoords, skew);
    float2 rotatedCoords = rotate2D(input, skewedCoords, rotation * (2.0f * M_PI));
    float2 translatedCoords = translate2D(input, rotatedCoords, translate);
    uint4 color = read_imageui(input, samplerIn, translatedCoords);
    write_imageui(output, gid, color);
}
