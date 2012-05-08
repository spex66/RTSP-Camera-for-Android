/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
#ifndef OSCL_MEM_H_INCLUDED
#define OSCL_MEM_H_INCLUDED

#include "oscl_types.h"

#define OSCLMemSizeT size_t

#define oscl_memcpy(dest, src, count)       memcpy((void *)(dest), (const void *)(src), (OSCLMemSizeT)(count))
#define oscl_memset(dest, ch, count)        memset((void *)(dest), (unsigned char)(ch), (OSCLMemSizeT)(count))
#define oscl_memmove(dest, src, bytecount)  memmove((void *)(dest), (const void *)(src), (OSCLMemSizeT)(bytecount))
#define oscl_memcmp(buf1, buf2, count)      memcmp( (const void *)(buf1), (const void *)(buf2), (OSCLMemSizeT)(count))
#define oscl_malloc(size)                      malloc((OSCLMemSizeT)(size))
#define oscl_free(memblock)                 free((void *)(memblock))
#define OSCL_ARRAY_DELETE(ptr)              delete [] ptr
#define OSCL_ARRAY_NEW(T, count)            new T[count]
#define OSCL_DELETE(memblock)               delete memblock
#define OSCL_NEW(arg)                       new arg

#endif // OSCL_MEM_H_INCLUDED

