const sampler_t samplerIn = 
  CLK_NORMALIZED_COORDS_FALSE | 
  CLK_ADDRESS_CLAMP | 
  CLK_FILTER_NEAREST;

__kernel void main (
  __read_only image2d_t input,
  __write_only image2d_t output
) {
  int2 pos = (int2)(get_global_id(0), get_global_id(1));
  uint4 pixel = read_imageui(input, samplerIn, pos);
  write_imageui(output, pos, pixel);
}
