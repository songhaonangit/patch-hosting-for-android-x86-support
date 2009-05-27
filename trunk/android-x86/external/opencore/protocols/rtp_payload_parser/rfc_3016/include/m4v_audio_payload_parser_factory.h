/* ------------------------------------------------------------------
 * Copyright (C) 2008 PacketVideo
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
///////////////////////////////////////////////////////////////////////////////
//
// m4v_audio_payload_parser_factory.h
//
///////////////////////////////////////////////////////////////////////////////

#ifndef M4V_AUDIO_PAYLOAD_PARSER_FACTORY_H_INCLUDED
#define M4V_AUDIO_PAYLOAD_PARSER_FACTORY_H_INCLUDED

#ifndef PAYLOAD_PARSER_FACTORY_H_INCLUDED
#include "payload_parser_factory.h"
#endif

class M4VAudioPayloadParserFactory : public IPayloadParserFactory
{
        OSCL_IMPORT_REF virtual IPayloadParser* createPayloadParser();
        OSCL_IMPORT_REF virtual void destroyPayloadParser(IPayloadParser* parser);
        virtual ~M4VAudioPayloadParserFactory()
        {
            ;
        }
};

#endif // M4V_AUDIO_PAYLOAD_PARSER_FACTORY_H_INCLUDED

