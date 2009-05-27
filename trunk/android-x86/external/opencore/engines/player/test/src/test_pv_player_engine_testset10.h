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
#ifndef TEST_PV_PLAYER_ENGINE_TESTSET10_H_INCLUDED
#define TEST_PV_PLAYER_ENGINE_TESTSET10_H_INCLUDED

/**
 *  @file test_pv_player_engine_testset10.h
 *  @brief This file contains the class definitions for the tenth set of
 *         test cases for PVPlayerEngine - Generic(local, PDL & streaming) delete tests
 *
 */

#ifndef TEST_PV_PLAYER_ENGINE_H_INCLUDED
#include "test_pv_player_engine.h"
#endif

#ifndef TEST_PV_PLAYER_ENGINE_CONFIG_H_INCLUDED
#include "test_pv_player_engine_config.h"
#endif

#ifndef PVMF_STREAMING_DATA_SOURCE_H_INCLUDED
#include "pvmf_streaming_data_source.h"
#endif


#ifndef PVMF_DOWNLOAD_DATA_SOURCE_H_INCLUDED
#include "pvmf_download_data_source.h"
#endif
#ifndef PVMF_SOURCE_CONTEXT_DATA_H_INCLUDED
#include "pvmf_source_context_data.h"
#endif


#define AMR_MPEG4_RTSP_URL "rtsp://test.3gp"
#define AMR_MPEG4_RTSP_URL_2 "rtsp://test.mp4"
#define H263_AMR_RTSP_URL "rtsp://test.3gp"
#define MPEG4_RTSP_URL "rtsp://test.3gp"
#define MPEG4_SHRT_HDR_RTSP_URL "rtsp://test.3gp"
#define AAC_RTSP_URL     "rtsp://test.3gp"
#define MPEG4_AAC_RTSP_URL "rtsp://test.3gp"
#define AMR_MPEG4_SDP_FILE "test.sdp"

class PVPlayerDataSourceURL;
class PVPlayerDataSink;
class PVPlayerDataSink;
class PVPlayerDataSinkFilename;
class PvmfFileOutputNodeConfigInterface;
class PvmiCapabilityAndConfig;

/*!
 *  Test cases to test DeletePlayer() call (right after/while processing) each state while playing an local/PDL/rtsp url
 *  - Data Source: Specified by user of test case
 *  - Data Sink(s): Video[FileOutputNode-test_player_genericdelete_video.dat]\n
 *                  Audio[FileOutputNode-test_player_genericdelete_audio.dat]
 *  - Sequence:
 *             -# CreatePlayer()
 *             -# DeletePlayer() is called after/in one of the following states based on the test case
 *             -# AddDataSource()
 *             -# Init()
 *             -# AddDataSink() (video)
 *             -# AddDataSink() (audio)
 *             -# Prepare()
 *             -# Start()
 *             -# Play 10 sec
 *             -# SetPlaybackRange() if seekEnable flag is made true
 *             -# Pause() if pauseresumeEnable flag is made true
 *             -# WAIT 10 sec
 *             -# Resume()
 *             -# Play until EOS
 *             -# Stop()
 *             -# RemoveDataSink() (video)
 *             -# RemoveDataSink() (audio)
 *             -# Reset()
 *             -# RemoveDataSource()
 *             -# DeletePlayer()
 *
 */
class pvplayer_async_test_genericdelete : public pvplayer_async_test_base
{
    public:
        pvplayer_async_test_genericdelete(PVPlayerAsyncTestParam aTestParam,
                                          PVMFFormatType aVideoSinkFormat,
                                          PVMFFormatType aAudioSinkFormat,
                                          uint32 aTestID,
                                          bool aPauseResumeEnable,
                                          bool aSeekEnable,
                                          bool aWaitForEOS,
                                          bool aCloaking,
                                          bool aCancelDuringPrepare,
                                          int lastState,
                                          bool aDeleteWhileProc)
                : pvplayer_async_test_base(aTestParam)
                , iPlayer(NULL)
                , iDataSource(NULL)
                , iDataSinkVideo(NULL)
                , iDataSinkAudio(NULL)
                , iIONodeVideo(NULL)
                , iIONodeAudio(NULL)
                , iMIOFileOutVideo(NULL)
                , iMIOFileOutAudio(NULL)
                , iCurrentCmdId(0)
                , iCancelAllCmdId(0)
                , iSessionDuration(0)
                , bcloaking(aCloaking)
                , oLiveSession(false)
                , iProtocolRollOver(false)
                , iProtocolRollOverWithUnknownURLType(false)
                , iPlayListURL(false)
                , iSourceContextData(NULL)
                , iStreamDataSource(NULL)
                , iPlayStarted(false)
                , iSeekDone(false)
        {
            iVideoSinkFormatType = aVideoSinkFormat;
            iAudioSinkFormatType = aAudioSinkFormat;
            oPauseResumeEnable = aPauseResumeEnable;
            oSeekEnable = aSeekEnable;
            oWaitForEOS = aWaitForEOS;
            oCancelDuringPrepare = aCancelDuringPrepare;
            iTestID = aTestID;
            iNumPlay = 0;
            iTargetNumPlay = 1;
            iNumBufferingStart = iNumBufferingComplete = iNumUnderflow = iNumDataReady = iNumEOS = 0;
            iDownloadOnly = iDownloadThenPlay = false;
            iEndState = (PVTestState)lastState;
            iDeleteWhileProc = aDeleteWhileProc;
        }

        ~pvplayer_async_test_genericdelete() {}

        void StartTest();
        void Run();

        void CommandCompleted(const PVCmdResponse& aResponse);
        void HandleErrorEvent(const PVAsyncErrorEvent& aEvent);
        void HandleInformationalEvent(const PVAsyncInformationalEvent& aEvent);

        PVTestState iState;
        PVTestState iEndState;
        bool iDeleteWhileProc;

        PVPlayerInterface* iPlayer;
        PVPlayerDataSourceURL* iDataSource;
        PVPlayerDataSink* iDataSinkVideo;
        PVPlayerDataSink* iDataSinkAudio;
        PVMFNodeInterface* iIONodeVideo;
        PVMFNodeInterface* iIONodeAudio;
        PvmiMIOControl* iMIOFileOutVideo;
        PvmiMIOControl* iMIOFileOutAudio;
        PVCommandId iCurrentCmdId;
        PVCommandId iCancelAllCmdId;


        void setMultiplePlayMode(uint32 aNum)
        {
            iMultiplePlay = true;
            iTargetNumPlay = aNum;
        }

        void setProtocolRollOverMode()
        {
            iProtocolRollOver = true;
        }

        void setProtocolRollOverModeWithUnknownURL()
        {
            iProtocolRollOverWithUnknownURLType = true;
        }

        void setPlayListMode()
        {
            iPlayListURL = true;
        }

    private:
        void HandleSocketNodeErrors(int32 aErr);
        void HandleRTSPNodeErrors(int32 aErr);
        void HandleStreamingManagerNodeErrors(int32 aErr);
        void HandleJitterBufferNodeErrors(int32 aErr);
        void HandleMediaLayerNodeErrors(int32 aErr);
        void HandleProtocolEngineNodeErrors(int32 aErr);


        void PrintMetadataInfo();

        PVMFFormatType iVideoSinkFormatType;
        PVMFFormatType iAudioSinkFormatType;
        OSCL_wHeapString<OsclMemAllocator> wFileName;
        oscl_wchar output[512];
        bool oPauseResumeEnable;
        bool oSeekEnable;
        bool oCancelDuringPrepare;
        bool oWaitForEOS;
        uint32 iTestID;

        PVPMetadataList iMetadataKeyList;
        Oscl_Vector<PvmiKvp, OsclMemAllocator> iMetadataValueList;
        int32 iNumValues;

        uint32 iSessionDuration;
        bool bcloaking;
        bool oLiveSession;

        bool iMultiplePlay;
        uint32 iNumPlay;
        uint32 iTargetNumPlay;

        bool iProtocolRollOver;
        bool iProtocolRollOverWithUnknownURLType;
        bool iPlayListURL;

        PvmiCapabilityAndConfig* iPlayerCapConfigIF;
        PvmiKvp* iErrorKVP;
        PvmiKvp iKVPSetAsync;
        OSCL_StackString<128> iKeyStringSetAsync;
        PVMFSourceContextData* iSourceContextData;

        PVMFStreamingDataSource* iStreamDataSource;

        //FTDL
        void CreateDownloadDataSource();
        PVMFDownloadDataSourceHTTP* iDownloadContextDataHTTP;
        int32 iDownloadMaxfilesize;
        OSCL_wHeapString<OsclMemAllocator> iDownloadURL;
        OSCL_wHeapString<OsclMemAllocator> iDownloadFilename;
        OSCL_HeapString<OsclMemAllocator> iDownloadProxy;
        OSCL_wHeapString<OsclMemAllocator> iDownloadConfigFilename;
        int32 iNumBufferingStart, iNumBufferingComplete, iNumUnderflow, iNumDataReady, iNumEOS;
        bool iDownloadOnly, iDownloadThenPlay;
        bool iContentTooLarge;
        bool iPlayStarted, iSeekDone;
};

#endif
