const __global uint ERODE = 0;
const __global uint DILATE = 1;
const __global uint4 BLACK = (uint4)(0, 0, 0, 255);
const __global uint4 WHITE = (uint4)(255, 255, 255, 255);

const sampler_t samplerIn = CLK_NORMALIZED_COORDS_FALSE |
                            CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__local bool structure(__read_only image2d_t input, int radius) {
  int2 pos = (int2)(get_global_id(0), get_global_id(1));
  uint4 pixel = read_imageui(input, samplerIn, pos);
  for (int x = pos.x - radius; x < pos.x + radius; x++) {
    for (int y = pos.y - radius; y < pos.y + radius; y++) {
      uint4 that = read_imageui(input, samplerIn, pos);
      if (that.x != 0 && pos.x != x && pos.y != y)
        return true;
    }
  }
  return false;
}

__kernel void main(__read_only image2d_t input, __write_only image2d_t output,
                   int radius, int mode) {
  int2 pos = (int2)(get_global_id(0), get_global_id(1));
  uint4 pixel = read_imageui(input, samplerIn, pos);
  bool alone = structure(input, radius);
  switch (mode) {
  case ERODE:
    if (!alone)
      write_imageui(output, pos, BLACK);
    break;
  case DILATE:
    if (!alone)
      write_imageui(output, pos, WHITE);
    break;
  default:
    write_imageui(output, pos, pixel);
    break;
  }
}
