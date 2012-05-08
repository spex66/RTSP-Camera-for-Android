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
#ifndef OSCL_BASE_H_INCLUDED
#define OSCL_BASE_H_INCLUDED

#include "oscl_config.h"
#include "oscl_types.h"
#include "oscl_error.h"

class OsclBase
{
    public:
        OSCL_IMPORT_REF  static void Init() {};
        OSCL_IMPORT_REF  static void Cleanup() {};
};

class OsclErrorTrap
{
    public:

        OSCL_IMPORT_REF  static void Init() {};
        OSCL_IMPORT_REF  static void Cleanup() {};
        OSCL_IMPORT_REF  static void leave(int) {};
};

class OsclMem
{
    public:
        OSCL_IMPORT_REF  static void Init() {};
        OSCL_IMPORT_REF  static void Cleanup() {};
};

class OsclRequestStatus
{
    public:
        OsclRequestStatus();
        OsclRequestStatus(int32 aVal)
        {
            (void)(aVal);
        };
        int32 operator=(int32 aVal);
        int32 operator==(int32 aVal) const;
        int32 operator!=(int32 aVal) const;
        int32 operator>=(int32 aVal) const;
        int32 operator<=(int32 aVal) const;
        int32 operator>(int32 aVal) const;
        int32 operator<(int32 aVal) const;
        int32 Int() const;
    private:
        int32 iStatus;
};

class OsclActiveObject
{
    public:
        /**
         * Scheduling priorities.
         */
        enum TPriority
        {
            /**
            A low priority, useful for active objects representing
            background processing.
            */
            EPriorityIdle = -100,
            /**
            A priority higher than EPriorityIdle but lower than EPriorityStandard.
            */
            EPriorityLow = -20,
            /**
            Most active objects will have this priority.
            */
            EPriorityStandard = 0,
            /**
            A priority higher than EPriorityStandard; useful for active objects
            handling user input.
            */
            EPriorityUserInput = 10,
            /**
            A priority higher than EPriorityUserInput.
            */
            EPriorityHigh = 20
        };

        /**
         * Constructor.
         * @param aPriority (input param): scheduling priority
         * @param name (inpup param): optional name for this AO.
         */
        OSCL_IMPORT_REF OsclActiveObject(int32 aPriority, const char name[]);

        /**
         * Destructor.
         */
        OSCL_IMPORT_REF virtual ~OsclActiveObject();

        /**
         * Set request active for this AO.
         * Will panic if the request is already active,
         * or the active object is not added to any scheduler,
         * or the calling thread context does not match
         * the scheduler thread.
         */
        OSCL_IMPORT_REF void SetBusy();

        /**
         * Return true if this AO is active,
         * false otherwise.
         */
        OSCL_IMPORT_REF bool IsBusy() const;

        /**
         * Set request active for this AO and set the status to pending.
         * PendForExec is identical to SetBusy, but it
         * additionally sets the request status to OSCL_REQUEST_PENDING.
         *
         */
        OSCL_IMPORT_REF void PendForExec();

        /**
         * Complate the active request for the AO.  Can be
         * called from any thread.
         * @param aStatus: request completion status.
         */
        OSCL_IMPORT_REF void PendComplete(int32 aStatus);


        /**
         * Add this AO to the current thread's scheduler.
         */
        OSCL_IMPORT_REF void AddToScheduler();

        /**
         * Return true if this AO is added to the scheduler,
         * false otherwise.
         */
        OSCL_IMPORT_REF bool IsAdded() const;

        /**
         * Remove this AO from its scheduler.
         * Will panic if the calling thread context does
         * not match the scheduling thread.
         * Cancels any active request before removing.
         */
        OSCL_IMPORT_REF void RemoveFromScheduler();

        /**
         * Deque is identical to RemoveFromScheduler
         * It's only needed to prevent accidental usage
         * of Symbian CActive::Deque.
         */
        OSCL_IMPORT_REF void Deque();

        /**
         * Complete this AO's request immediately.
         * If the AO is already active, this will do nothing.
         * Will panic if the AO is not acced to any scheduler,
         * or if the calling thread context does not match the
         * scheduling thread.
         */
        OSCL_IMPORT_REF void RunIfNotReady();

        /**
         * Cancel any active request.
         * If the request is active, this will call the DoCancel
         * routine, wait for the request to cancel, then set the
         * request inactive.  The AO will not run.
         * If the request is not active, it does nothing.
         * Request must be canceled from the same thread
         * in which it is scheduled.
         */
        OSCL_IMPORT_REF void Cancel();

        /**
        * Return scheduling priority of this active object.
        */
        OSCL_IMPORT_REF int32 Priority() const;

        /**
        * Request status access
        */
        OSCL_IMPORT_REF int32 Status()const;
        OSCL_IMPORT_REF void SetStatus(int32);
        OSCL_IMPORT_REF int32 StatusRef();

    protected:
        /**
         * Cancel request handler.
         * This gets called by scheduler when the request
         * is cancelled.  The default routine will complete
         * the request.  If any additional action is needed,
         * the derived class may override this.  If the derived class
         * does override DoCancel, it must complete the request.
         */
        //OSCL_IMPORT_REF virtual void DoCancel();

        /**
        * Run Error handler.
        * This gets called by scheduler when the Run routine leaves.
        * The default implementation simply returns the leave code.
        * If the derived class wants to handle errors from Run,
        * it may override this.  The RunError should return OsclErrNone
        * if it handles the error, otherwise it should return the
        * input error code.
        * @param aError: the leave code generated by the Run.
        */
        //OSCL_IMPORT_REF virtual int32 RunError(int32 aError);
};


class OsclTimerObject
{
    public:
        /**
         * Constructor.
         * @param aPriority (input param): scheduling priority
         * @param name (input param): optional name for this AO.
         */
        OSCL_IMPORT_REF OsclTimerObject(int32 aPriority, const char name[]);

        /**
         * Destructor.
         */

        //OSCL_IMPORT_REF virtual ~OsclTimerObject();

        /**
         * Add this AO to the current thread's scheduler.
         */
        OSCL_IMPORT_REF void AddToScheduler();

        /**
         * Return true if this AO is added to the scheduler,
         * false otherwise.
         */
        OSCL_IMPORT_REF bool IsAdded() const;

        /**
         * Remove this AO from its scheduler.
         * Will panic if the calling thread context does
         * not match the scheduling thread.
         * Cancels any active request before removing.
         */
        OSCL_IMPORT_REF void RemoveFromScheduler();

        /**
         * Deque is identical to RemoveFromScheduler
         * It's only needed to prevent accidental usage
         * of Symbian CActive::Deque.
         */
        OSCL_IMPORT_REF void Deque();

        /**
        * 'After' sets the request active, with request status
        * OSCL_REQUEST_STATUS_PENDING, and starts a timer.
        * When the timer expires, the request will complete with
        * status OSCL_REQUEST_ERR_NONE.
        * Must be called from the same thread in which the
        * active object is scheduled.
        * Will panic if the request is already active, the object
        * is not added to any scheduler, or the calling thread
        * does not match the scheduling thread.
        * @param anInterval: timeout interval in microseconds.
        */
        OSCL_IMPORT_REF void After(int32 aDelayMicrosec);

        /**
         * Complete the request after a time interval.
         * RunIfNotReady is identical to After() except that it
         * first checks the request status, and if it is already
         * active, it does nothing.
         *
         * @param aDelayMicrosec (input param): delay in microseconds.
         */
        OSCL_IMPORT_REF void RunIfNotReady(uint32 aDelayMicrosec = 0);

        /**
         * Set request active for this AO.
         * Will panic if the request is already active,
         * or the active object is not added to any scheduler,
         * or the calling thread context does not match
         * the scheduler thread.
         */
        OSCL_IMPORT_REF void SetBusy();

        /**
         * Return true if this AO is active,
         * false otherwise.
         */
        OSCL_IMPORT_REF bool IsBusy() const;

        /**
         * Cancel any active request.
         * If the request is active, this will call the DoCancel
         * routine, wait for the request to cancel, then set the
         * request inactive.  The AO will not run.
         * If the request is not active, it does nothing.
         * Request must be canceled from the same thread
         * in which it is scheduled.
         */
        OSCL_IMPORT_REF void Cancel();

        /**
        * Return scheduling priority of this active object.
        */
        OSCL_IMPORT_REF int32 Priority() const;
        /**
        * Request status access
        */
        OSCL_IMPORT_REF int32 Status()const;
        OSCL_IMPORT_REF void SetStatus(int32);
        OSCL_IMPORT_REF int32 StatusRef();

    protected:
        /**
         * Cancel request handler.
         * This gets called by scheduler when the request
         * is cancelled.  The default routine will cancel
         * the timer.  If any additional action is needed,
         * the derived class may override this.  If the
         * derived class does override this, it should explicitly
         * call OsclTimerObject::DoCancel in its own DoCancel
         * routine.
         */
        //OSCL_IMPORT_REF virtual void DoCancel();

        /**
        * Run Error handler.
        * This gets called by scheduler when the Run routine leaves.
        * The default implementation simply returns the leave code.
        * If the derived class wants to handle errors from Run,
        * it may override this.  The RunError should return OsclErrNone
        * if it handles the error, otherwise it should return the
        * input error code.
        * @param aError: the leave code generated by the Run.
        */
        //OSCL_IMPORT_REF virtual int32 RunError(int32 aError);
};

#endif // OSCL_BASE_H_INCLUDED

