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
/*********************************************************************************/
/* Description: Created for abstracting out OSCL such that the code can be used */
/*          by both V3 and V4 OSCL library. This file is for V4.                */
/*********************************************************************************/

#ifndef _M4VENC_OSCL_H_
#define _M4VENC_OSCL_H_


#define OSCL_DISABLE_WARNING_CONV_POSSIBLE_LOSS_OF_DATA
#include "osclconfig_compiler_warnings.h"

#include "oscl_mem.h"

#define M4VENC_MALLOC(size)   oscl_malloc(size)
#define M4VENC_FREE(ptr)                oscl_free(ptr)

#define M4VENC_MEMSET(ptr,val,size)     oscl_memset(ptr,val,size)
#define M4VENC_MEMCPY(dst,src,size)     oscl_memcpy(dst,src,size)

#include "oscl_math.h"
#define M4VENC_LOG(x)                   oscl_log(x)
#define M4VENC_SQRT(x)                  oscl_sqrt(x)
#define M4VENC_POW(x,y)                 oscl_pow(x,y)

#define M4VENC_HAS_SYMBIAN_SUPPORT  OSCL_HAS_SYMBIAN_SUPPORT

#endif //_M4VENC_OSCL_H_
