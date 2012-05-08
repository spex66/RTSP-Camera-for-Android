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
#ifndef PVM4VDECODER_FACTORY_H_INCLUDED
#define PVM4VDECODER_FACTORY_H_INCLUDED

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef OSCL_MEM_H_INCLUDED
#include "oscl_mem.h"
#endif

class PVVideoDecoderInterface;

class PVM4VDecoderFactory
{
    public:
        /**
         * Creates an instance of a PVM4VDecoder. If the creation fails, this function will leave.
         *
         * @returns A pointer to an instance of PVM4VDecoder as PVVideoDecoderInterface reference or leaves if instantiation fails
         **/
        OSCL_IMPORT_REF static PVVideoDecoderInterface* CreatePVM4VDecoder(void);

        /**
         * Deletes an instance of PVM4VDecoder and reclaims all allocated resources.
         *
         * @param aVideoDec The PVM4VDecoder instance to be deleted
         * @returns A status code indicating success or failure of deletion
         **/
        OSCL_IMPORT_REF static bool DeletePVM4VDecoder(PVVideoDecoderInterface* aVideoDec);
};

#endif

