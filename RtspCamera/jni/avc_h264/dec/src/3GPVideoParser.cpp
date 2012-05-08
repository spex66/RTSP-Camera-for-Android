/*
 * Copyright (C) 2009 OrangeLabs
 * 3GPVideoParser.cpp
 *
 *  Created on: 12 août 2009
 *      Author: rglt1266
 */
#define LOG_TAG "3GPPSampleReader"
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <limits.h>
#include "3GPVideoParser.h"

/* Variables */
FILE* f = NULL; // File to parse

uint32 TimeScale = 0; // Ticks per second
uint32 VideoLength = 0; // Video length (time)
uint32 VideoWidth = 0;
uint32 VideoHeight = 0;
char VideoCodec[5]; // Codec type: d263/mp4v....

uint32 moovAtomPtr = 0;
uint32 moovAtomSize = 0;
uint32 trakAtomPtr = 0;
uint32 trakAtomSize = 0;

/* Buffers and pointers*/
uint8* moovBuff = 0;
uint8* sttsPtr = 0;
uint8* stcoPtr = 0;
uint8* stszPtr = 0;
uint8* stscPtr = 0;
uint8* stsdPtr = 0;
Sample* samplePtr = 0;

/**
 * Endien convert
 */
uint32 EndienConvert (uint32 input){
	return ((input & 0xFF) << 24) | ((input & 0xFF00) << 8) |	((uint32)(input & 0xFF0000) >> 8) | ((uint32)(input & 0xFF000000) >> 24);
}

/**
 * Get a uint32 value at a precised position in a uint8 buffer
 */
uint32 getUint32FromUint8Buffer (uint8* buffer,uint32 offset){
	return ( ((buffer[offset]<<24)& 0xff000000) | ((buffer[offset+1]<<16)& 0xff0000) | ((buffer[offset+2]<<8)& 0xff00) | ((buffer[offset+3])& 0xff));
}

/**
 * Find a particular value in a uint8 buffer reading uint32
 */
int32 findAtom (uint8* buffer,uint32 bufferSize, uint32 valueToFind){
	uint32 tmp;
	uint32 i = 0;
	for (i=0;i<(bufferSize-4);i++){
		tmp = getUint32FromUint8Buffer(buffer,i);
		if (tmp == valueToFind){
			return i-4;
		}
	}
	return VPAtomError;
}

/**
 * Find a particular value in a uint32 buffer
 */
int32 findAtom (uint32* buffer,uint32 bufferSize, uint32 valueToFind){
	uint32 i = 0;
	for (i=0;i<(bufferSize);i++){
		if (EndienConvert(buffer[i]) == valueToFind){
			return i;
		}
	}
	return VPAtomError;
}

/**
* Cleanup the parser
*
* @return error code
*/
int cleanupParser(void){
	/* Clean atom info */
	free(moovBuff);
	VideoWidth = 0;
	VideoHeight = 0;
	VideoCodec[0] = '\0';
	VideoLength = 0;
	return VPAtomSucces;
}

/**
* Init the parser
*
* @param filePath path of the file to read
* @param width check if the video width is correct
* @param heigth check if the video height is correct
* @return error code
*/
int Init3GPVideoParser (char *filePath){
	uint32 anAtomSize = 0;
	uint32 anAtomType = 0;
	uint32 trakOffset = 0;

	int32 pos = 0;
	int32 fileSize;

	/* Load file */
	f = fopen(filePath,"r");
	if (f == NULL) {
	  return VPAtomError;
	}
	fseek( f, 0L, SEEK_END );
	fileSize = ftell( f );
	if (fileSize <= 8 ) return VPAtomError; // File is too small !

	/* Check if file format is correct ie it's a 3gp file*/
	fseek(f,4,SEEK_SET);
	fread(&anAtomType,sizeof(uint32),1,f);
	anAtomType = EndienConvert(anAtomType);
	if (anAtomType != AtomFtyp) return VPAtomError;

	/* Start parsing from begining*/
	rewind (f);

	// Find Moov Atom
	while (ftell(f)<fileSize){
		fread(&anAtomSize,sizeof(uint32),1,f);
		anAtomSize = EndienConvert(anAtomSize);
		fread(&anAtomType,sizeof(uint32),1,f);
		anAtomType = EndienConvert(anAtomType);
		if (anAtomType == AtomMoov){
			moovAtomPtr=ftell(f)-8;
			moovAtomSize=anAtomSize;
		}
		// Switch to next Atom
		fseek(f,anAtomSize-8,SEEK_CUR);/* -8 is because we already read 2*4 Bytes of this Atom*/
	}

	/* Copy moov to buffer */
	moovBuff = (uint8*)malloc(moovAtomSize);
	fseek(f,moovAtomPtr,SEEK_SET);
	for (uint32 j=0;j<(moovAtomSize);j++){
		fread(&moovBuff[j],1,1,f);
	}

	// Find trak(s) Atom
	pos = findAtom(moovBuff,moovAtomSize,AtomTrak);
	while (pos > 0) {
		int32 trakSize = getUint32FromUint8Buffer(moovBuff,pos);
		if (findAtom(moovBuff+pos,trakSize,AtomVmhd)){
			trakAtomPtr = moovAtomPtr+pos;
			trakAtomSize = trakSize;
			break;
		} else {
			// This is not the videotrack
		}
		// Trying to find new trak
		pos = findAtom(moovBuff+pos,moovAtomSize-pos,AtomTrak);
	}
	if (trakAtomPtr == 0) {
	    return VPAtomError;
	}


	trakOffset = trakAtomPtr - moovAtomPtr;

	// Find MDHD
	pos = findAtom(moovBuff+trakOffset,trakAtomSize,AtomMdhd);
	if (pos > 0){
		uint8* Ptr = moovBuff + trakOffset + pos + 16; // Skip Atom size and Atom name
		TimeScale = getUint32FromUint8Buffer(Ptr,4);
		VideoLength = getUint32FromUint8Buffer(Ptr,8);
	} else {
		return VPAtomError;
	}

	// Find STTS
	pos = findAtom(moovBuff+trakOffset,trakAtomSize,AtomStts);
	if (pos > 0){
		sttsPtr = moovBuff + trakOffset + pos + 16; // Skip Atom size and Atom name
	} else {
		return VPAtomError;
	}

	// Find STSZ
	pos = findAtom(moovBuff+trakOffset,trakAtomSize,AtomStsz);
	if (pos > 0){
		stszPtr = moovBuff + trakOffset + pos + 20; // Skip Atom size and Atom name
	} else {
		return VPAtomError;
	}
	// Find STCO
	pos = findAtom(moovBuff+trakOffset,trakAtomSize,AtomStco);
	if (pos > 0){
		stcoPtr = moovBuff + trakOffset + pos + 16; // Skip Atom size, Atom name, ...
	} else {
		return VPAtomError;
	}
	// Find STSC
	pos = findAtom(moovBuff+trakOffset,trakAtomSize,AtomStsc);
	if (pos > 0){
		stscPtr = moovBuff + trakOffset + pos + 16; // Skip Atom size, Atom name, ...
	} else {
		return VPAtomError;
	}
	// Find STSD
	pos = findAtom(moovBuff+trakOffset,trakAtomSize,AtomStsd);
	if (pos > 0){
		stsdPtr = moovBuff + trakOffset + pos + 16; // Skip Atom size and Atom name
		VideoWidth = (getUint32FromUint8Buffer(stsdPtr,32)>>16) & 0xFFFF;
		VideoHeight = getUint32FromUint8Buffer(stsdPtr,32) & 0xFFFF;
		VideoCodec[0] = *(stsdPtr+90);
		VideoCodec[1] = *(stsdPtr+91);
		VideoCodec[2] = *(stsdPtr+92);
		VideoCodec[3] = *(stsdPtr+93);
		VideoCodec[4]= '\0';
	} else {
	      return VPAtomError;
	}


	/**
	 * Prepare Sample list
	 */
	uint32 countChunk = 0; // Total number of chunk
	uint32 currChunk=0; // Counter for current chunk
	uint32 currChunkInStsc=0; // Current chunk described in stsc Atom
	uint32 ChunkAddr = 0; // Current chunk offset
	uint32 countSample = 0; // Counter for sample in a chunk
	uint32 currSample = 0; // Counter for current sample (/total sample in file)
	uint32 SamplePerChunk = 0; // Value sample per chunk
	uint32 currStscPos = 0; // Current stsc table
	uint32 Offset = 0; // Offset from ChunkAddr to sample data start
	int32 currSttsPos = 0;
	uint32 SameTimestampCount = 0; // For case where n sample have the same timestamp
	uint32 temp;
	Sample* currSamplePtr = 0; // Pointer to current Sample
	Sample* aSample = 0; // Current Sample element
	bool initList = false; // Boolean changed after first sample is read

	/* Get "Number of entries" field of stco atom */
	countChunk = getUint32FromUint8Buffer(stcoPtr-4,0);
	/* Init currChunk */
	currChunkInStsc = getUint32FromUint8Buffer(stscPtr,currStscPos*12);

	for (currChunk=0;currChunk<countChunk;currChunk++){
		ChunkAddr = getUint32FromUint8Buffer(stcoPtr,currChunk*4);
		if (currChunkInStsc == currChunk+1){
			SamplePerChunk = getUint32FromUint8Buffer(stscPtr,currStscPos*12+4);
			currStscPos++;
			currChunkInStsc = getUint32FromUint8Buffer(stscPtr,currStscPos*12);
		} else {
			// Repeat old value
		}
		Offset = 0;
		for (countSample=0;countSample<SamplePerChunk;countSample++){
			/* Malloc a new sample */
			aSample = (Sample*)malloc(sizeof(Sample));
			/* Get sample size */
			aSample->size = getUint32FromUint8Buffer(stszPtr,currSample*4);
			currSample++;
			/* Get sample addr */
			aSample->addr = ChunkAddr + Offset;
			Offset = Offset + aSample->size;
			/* Get sample timestamp */
			if (SameTimestampCount == 0){
				// Read new stts element
				SameTimestampCount = getUint32FromUint8Buffer(sttsPtr,currSttsPos*8);
				currSttsPos++;
			}
			temp = getUint32FromUint8Buffer(sttsPtr,(currSttsPos-1)*8+4);
			aSample->timestamp = (uint32)((temp*1000)/TimeScale);
			SameTimestampCount--;
			/* Set next to NULL */
			aSample->next = NULL;
			/* Update the sample list */
			if (initList == false){
				samplePtr = aSample;
				currSamplePtr = aSample;
				initList = true;
			} else {
				currSamplePtr->next = aSample;
				currSamplePtr = aSample;
				currSamplePtr->next = NULL;
			}
		}
	}
	return VPAtomSucces;
}

/**
* Get Videoframe
*
* @param aOutBuffer buffer to write the videoframe
* @param aBufferSize size of the buffer
* @param aTimestamp timestamp
* @return error code for overrun buffer
*/
int getFrame (uint8* aOutBuffer,uint32* aBufferSize, uint32* aTimestamp){
	// Temp sample to free data
	Sample* tmp;
	if (samplePtr != NULL){
		if (aOutBuffer == NULL || f==NULL){
		    return VPAtomError;
		}
		fseek(f,samplePtr->addr,SEEK_SET);
		if (fread(aOutBuffer,1,samplePtr->size,f) != samplePtr->size){
			return VPAtomError;
		}
		*aTimestamp = samplePtr->timestamp;
		*aBufferSize = samplePtr->size;
		/* Free the sample */
		tmp = samplePtr;
		samplePtr = samplePtr->next;
		free(tmp);
		return VPAtomSucces;
	} else {
		aOutBuffer = NULL;
		*aBufferSize = 0;
		*aTimestamp = 0;
		return VPAtomError;
	}
}

/**
 * Release file by closing it
 *
 * @return error code
 */
int release(){
	if (f != NULL){
		fclose(f);
	}
	return cleanupParser();
}

/**
 * Get the video duration
 *
 * @return video duration in seconds ( last 3 digits are ms)
 */
uint32 getVideoDuration (){
	uint32 retValue = 0;
	retValue = ((VideoLength/TimeScale)*1000)+(VideoLength%TimeScale);
	return retValue;
}

/**
 * Get the video codec
 *
 * @return video codec string
 */
char* getVideoCodec (){
	return VideoCodec;
}

/**
 * Get video width
 *
 * @return video width
 */
uint32 getVideoWidth (){
	return VideoWidth;
}

/**
 * Get the video height
 *
 * @return video height
 */
uint32 getVideoHeight(){
	return VideoHeight;
}
