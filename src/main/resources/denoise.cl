const sampler_t samplerIn = 
    CLK_NORMALIZED_COORDS_FALSE | 
    CLK_ADDRESS_CLAMP | 
    CLK_FILTER_NEAREST;

float4 applyGaussianFilter(float4 pixel, float filterStrength) {
    float filter[3][3] = {
        {1.0f, 2.0f, 1.0f},
        {2.0f, 4.0f, 2.0f},
        {1.0f, 2.0f, 1.0f}
    };

    float sum = 0.0f;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            sum += filter[i][j];
        }
    }
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            filter[i][j] /= sum;
        }
    }

    float4 result = (float4)(0.0f, 0.0f, 0.0f, 0.0f);
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            result += filter[i][j] * pixel;
        }
    }

    result *= filterStrength;

    return result;
}

__kernel void main(
    __read_only image2d_t inputImage,
    __write_only image2d_t outputImage,
    const int patchSize,
    const int searchWindowSize,
    const float filterStrength
) {
    int gidX = get_global_id(0);
    int gidY = get_global_id(1);
    int width = get_image_width(inputImage);
    int height = get_image_height(inputImage);
    int pixelIndex = gidY * width + gidX;

    int halfPatchSize = patchSize / 2;
    int halfSearchWindowSize = searchWindowSize / 2;

    float4 result = (float4)(0.0f, 0.0f, 0.0f, 0.0f);
    float totalWeight = 0.0f;

    for (int i = -halfSearchWindowSize; i <= halfSearchWindowSize; ++i) {
        for (int j = -halfSearchWindowSize; j <= halfSearchWindowSize; ++j) {
            int neighborX = clamp(gidX + i, 0, width - 1);
            int neighborY = clamp(gidY + j, 0, height - 1);

            float weight = 0.0f;

            for (int m = -halfPatchSize; m <= halfPatchSize; ++m) {
                for (int n = -halfPatchSize; n <= halfPatchSize; ++n) {
                    int x = clamp(gidX + m, 0, width - 1);
                    int y = clamp(gidY + n, 0, height - 1);
                    float4 pixelA = read_imagef(inputImage, samplerIn, (int2)(x, y));
                    float4 pixelB = read_imagef(inputImage, samplerIn, (int2)(x + i, y + j));
                    float diffX = pixelA.x - pixelB.x;
                    float diffY = pixelA.y - pixelB.y;
                    float diffZ = pixelA.z - pixelB.z;
                    float diff = diffX * diffX + diffY * diffY + diffZ * diffZ;                    
                    weight += exp(-diff * diff * filterStrength);
                }
            }

            int pixelIndex = neighborY * width + neighborX;
            float4 pixel = read_imagef(inputImage, samplerIn, (int2)(neighborX, neighborY));
            float4 weightedPixel = pixel * weight;
            result += weightedPixel;
            totalWeight += weight;
        }
    }
    
    float4 filteredPixel = result / totalWeight;
    int2 pixelCoord = (int2)(gidX, gidY);
    write_imagef(outputImage, pixelCoord, filteredPixel);
}
    