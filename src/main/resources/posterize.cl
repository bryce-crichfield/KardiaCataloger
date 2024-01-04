const sampler_t samplerIn =
    CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST;

__kernel void main(__read_only image2d_t input, __write_only image2d_t output,
                   int alpha, int beta, int invert) {
  int2 pos = (int2)(get_global_id(0), get_global_id(1));
  uint4 pixel = read_imageui(input, samplerIn, pos);
  float a = 255 / alpha;
  float b = beta - 255;
  pixel.x = a * pixel.x + b;
  pixel.y = a * pixel.y + b;
  pixel.z = a * pixel.z + b;
  float value = (pixel.r * 0.299f + pixel.g * 0.587f + pixel.b * 0.114f);
  float out = clamp(value, (float)0, (float)254);
  if (invert) {
    out = 255.0f - out;
  }
  write_imageui(output, pos, (uint4)(out, out, out, 1.0f));
}
