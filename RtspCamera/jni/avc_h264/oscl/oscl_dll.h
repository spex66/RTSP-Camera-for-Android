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
#ifndef OSCL_DLL_H_INCLUDED
#define OSCL_DLL_H_INCLUDED

#define OSCL_DLL_ENTRY_POINT() void oscl_dll_entry_point() {}


/**
 * Default DLL entry/exit point function.
 *
 * The body of the DLL entry point is given.  The macro
 * only needs to be declared within the source file.
 *
 * Usage :
 *
 * OSCL_DLL_ENTRY_POINT_DEFAULT()
 */

#define OSCL_DLL_ENTRY_POINT_DEFAULT()



#endif // OSCL_DLL_H_INCLUDED

