const sampler_t samplerIn = CLK_NORMALIZED_COORDS_FALSE |
                            CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void main(__read_only image2d_t input_image,
                         __write_only image2d_t output_image,
                         const int patch_size,
                         const int search_window,
                         const float h,
                         const float sigma) {

    const int gid_x = get_global_id(0);
    const int gid_y = get_global_id(1);

    const int half_patch = patch_size / 2;

    float nlm_pixel = 0.0f;
    float total_weight = 0.0f;

    for (int i = gid_x - search_window; i <= gid_x + search_window; ++i) {
        for (int j = gid_y - search_window; j <= gid_y + search_window; ++j) {

            if (i >= half_patch && i < get_image_width(input_image) - half_patch &&
                j >= half_patch && j < get_image_height(input_image) - half_patch) {

                float weight = 0.0f;

                for (int pi = -half_patch; pi <= half_patch; ++pi) {
                    for (int pj = -half_patch; pj <= half_patch; ++pj) {
                        float diff = read_imagef(input_image, samplerIn, (float2)(i + pi, j + pj)).x -
                                     read_imagef(input_image, samplerIn, (float2)(gid_x + pi, gid_y + pj)).x;
                        weight += exp(-diff * diff / (h * h * sigma * sigma));
                    }
                }

                float pixel_value = read_imagef(input_image, samplerIn, (float2)(i, j)).x;
                nlm_pixel += weight * pixel_value;
                total_weight += weight;
            }
        }
    }

    write_imagef(output_image, (int2)(gid_x, gid_y), nlm_pixel / total_weight);
}