/*
 * Copyright (C) 2009 OrangeLabs
 * 3GPVideoParser.h
 *
 *  Created on: 12 août 2009
 *      Author: rglt1266
 */

#ifndef _3GPVIDEOPARSER_H_
#define _3GPVIDEOPARSER_H_

/* Define new types */
typedef unsigned char uint8;
typedef unsigned short uint16;
typedef short int16;
typedef unsigned long uint32;
typedef long int32;

#define DEBUG 1;

/* Define important atoms 4Bytes code (char)*/
#define	AtomFtyp 0x66747970 /* File type compatibility atom */
#define	AtomMdat 0x6D646174 /* Movie sample data atom */
#define	AtomMoov 0x6D6F6F76 /* Movie ressource metadata atom */
#define	AtomMdhd 0x6D646864 /* Video media information header atom */
#define	AtomMvhd 0x6D766864 /* Video media information header atom */
#define	AtomStts 0x73747473 /* Time-to-sample atom */
#define	AtomStco 0x7374636F /* Sample-to-chunck atom */
#define	AtomTrak 0x7472616B /* Trak atom */
#define	AtomStsz 0x7374737A /* Sample size atom */
#define AtomStsc 0x73747363 /* Nb of sample per chunck */
#define AtomStsd 0x73747364 /* Nb of sample per chunck */
#define AtomVmhd 0x766D6864 /* Identifier of a video track */

/* Define error codes */
#define VPAtomError 0
#define VPAtomSucces 1

typedef struct {
	uint32 ptr;
	uint32 size;
} Atom;

struct sample {
	uint32 addr;
	uint32 size;
	uint32 timestamp;
	struct sample *next;
};
typedef struct sample Sample;

int Init3GPVideoParser (char *);
int release();
int getFrame (uint8*,uint32*, uint32*);
uint32 getVideoDuration();
uint32 getVideoWidth();
uint32 getVideoHeight();
char* getVideoCodec();

#endif /* 3GPVIDEOPARSER_H_ */
