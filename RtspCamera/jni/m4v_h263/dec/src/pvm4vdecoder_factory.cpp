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
/**
 * @file pvm4vdecoder_factory.cpp
 * @brief Singleton factory for PVM4VDecoder
 */

#include "oscl_base.h"

#include "pvm4vdecoder.h"
#include "pvm4vdecoder_factory.h"

#include "oscl_error_codes.h"
#include "oscl_exception.h"

// Use default DLL entry point
#include "oscl_dll.h"

OSCL_DLL_ENTRY_POINT_DEFAULT()


////////////////////////////////////////////////////////////////////////////
OSCL_EXPORT_REF PVVideoDecoderInterface* PVM4VDecoderFactory::CreatePVM4VDecoder()
{
    PVVideoDecoderInterface* videodec = NULL;
    videodec = PVM4VDecoder::New();
    if (videodec == NULL)
    {
        OSCL_LEAVE(OsclErrNoMemory);
    }
    return videodec;
}

////////////////////////////////////////////////////////////////////////////
OSCL_EXPORT_REF bool PVM4VDecoderFactory::DeletePVM4VDecoder(PVVideoDecoderInterface* aVideoDec)
{
    if (aVideoDec)
    {
        OSCL_DELETE(aVideoDec);
        return true;
    }

    return false;
}

