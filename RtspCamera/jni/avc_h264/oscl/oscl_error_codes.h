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

#ifndef OSCL_ERROR_CODES_H_INCLUDED
#define OSCL_ERROR_CODES_H_INCLUDED


/** Leave Codes
*/
typedef int32 OsclLeaveCode;

#define OsclErrNone 0
#define OsclErrGeneral 100
#define OsclErrNoMemory 101
#define OsclErrCancelled 102
#define OsclErrNotSupported 103
#define OsclErrArgument 104
#define OsclErrBadHandle 105
#define OsclErrAlreadyExists 106
#define OsclErrBusy 107
#define OsclErrNotReady 108
#define OsclErrCorrupt 109
#define OsclErrTimeout 110
#define OsclErrOverflow 111
#define OsclErrUnderflow 112
#define OsclErrInvalidState 113
#define OsclErrNoResources 114

/** For backward compatibility with old definitions
*/
#define OSCL_ERR_NONE OsclErrNone
#define OSCL_BAD_ALLOC_EXCEPTION_CODE OsclErrNoMemory

/** Return Codes
*/
typedef int32 OsclReturnCode;

#define  OsclSuccess 0
#define  OsclPending 1
#define  OsclFailure -1

#endif

/*! @} */
