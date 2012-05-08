/*
 * yuv2rgb.cpp
 *
 *  Created on: 29 juil. 2009
 *      Author: rglt1266
 */
#include <stdio.h>
#include "yuv2rgb.h"

int convert (int width,int height, uint8 *in,uint32 *out){
	uint8 *pY;
	uint8 *pU;
	uint8 *pV;
	int Y,U,V;
	int i,j;
	int R,G,B,Cr,Cb;

	/* Init */
	pY = in;
	pU = in + (width*height);
	pV = pU + (width*height/4);

	for(i=0;i<height;i++){
		for(j=0;j<width;j++){
			/* YUV values uint */
			Y=*((pY)+ (i*width) + j);
			U=*( pU + (j/2) + ((width/2)*(i/2)));
			V=*( pV + (j/2) + ((width/2)*(i/2)));
			/* RBG values */
			Cr = V-128;
			Cb = U-128;
			R = Y + ((359*Cr)>>8);
			G = Y - ((88*Cb+183*Cr)>>8);
			B = Y + ((454*Cb)>>8);
			if (R>255)R=255; else if (R<0)R=0;
			if (G>255)G=255; else if (G<0)G=0;
			if (B>255)B=255; else if (B<0)B=0;

			/* Write data */
			out[((i*width) + j)]=((((R & 0xFF) << 16) | ((G & 0xFF) << 8) | (B & 0xFF))& 0xFFFFFFFF);
		}
	}
	return 1;
}
