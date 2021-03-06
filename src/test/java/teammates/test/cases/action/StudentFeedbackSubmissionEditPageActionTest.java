package teammates.test.cases.action;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.EntityNotFoundException;
import teammates.common.exception.NullHttpParameterException;
import teammates.common.util.Const;
import teammates.common.util.StringHelper;
import teammates.logic.core.StudentsLogic;
import teammates.storage.api.FeedbackSessionsDb;
import teammates.storage.api.StudentsDb;
import teammates.test.driver.AssertHelper;
import teammates.ui.controller.Action;
import teammates.ui.controller.RedirectResult;
import teammates.ui.controller.ShowPageResult;
import teammates.ui.controller.StudentFeedbackSubmissionEditPageAction;

/**
 * SUT: {@link StudentFeedbackSubmissionEditPageAction}.
 */
public class StudentFeedbackSubmissionEditPageActionTest extends BaseActionTest {

    @Override
    protected String getActionUri() {
        return Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE;
    }

    @Override
    protected void prepareTestData() {
        // see setup()
    }

    @BeforeMethod
    public void setup() throws Exception {
        dataBundle = loadDataBundle("/StudentFeedbackSubmissionEditPageActionTest.json");
        removeAndRestoreDataBundle(dataBundle);
        addUnregStudentToCourse1();
    }

    @AfterMethod
    public void tearDown() {
        StudentsLogic.inst().deleteStudentCascade("idOfTypicalCourse1", "student6InCourse1@gmail.tmt");
    }

    @Test
    public void testExecuteAndPostProcess_registeredStudentAccessSoftDeletedSession_shouldNotAccessAndRedirect()
            throws Exception {
        FeedbackSessionAttributes session1InCourse1 = dataBundle.feedbackSessions.get("session1InCourse1");
        StudentAttributes student1InCourse1 = dataBundle.students.get("student1InCourse1");

        logic.moveFeedbackSessionToRecycleBin(session1InCourse1.getFeedbackSessionName(), session1InCourse1.getCourseId());

        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        String[] params = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        Action pageAction = getAction(params);
        RedirectResult redirectResult = getRedirectResult(pageAction);

        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, false, "student1InCourse1"),
                redirectResult.getDestinationWithParams());
        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_DELETED_NO_ACCESS,
                redirectResult.getStatusMessage());
    }

    @Test(expectedExceptions = EntityNotFoundException.class,
            expectedExceptionsMessageRegExp = ".*unregistered student trying to access non-existent session.*")
    public void testExecuteAndPostProcess_unregisteredStudentAccessSoftDeletedSession_shouldNotAccessAndExceptionThrow()
            throws Exception {
        FeedbackSessionAttributes session1InCourse1 = dataBundle.feedbackSessions.get("session1InCourse1");
        StudentAttributes unregStudent =
                logic.getStudentForEmail("idOfTypicalCourse1", "student6InCourse1@gmail.tmt");

        logic.moveFeedbackSessionToRecycleBin(session1InCourse1.getFeedbackSessionName(), session1InCourse1.getCourseId());

        gaeSimulation.logoutUser();

        String[] params = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.REGKEY, StringHelper.encrypt(unregStudent.key),
                Const.ParamsNames.STUDENT_EMAIL, unregStudent.email
        };

        Action pageAction = getAction(params);
        getRedirectResult(pageAction);
    }

    @Override
    @Test
    public void testExecuteAndPostProcess() throws Exception {
        StudentAttributes student1InCourse1 = dataBundle.students.get("student1InCourse1");
        StudentAttributes unregStudent = StudentAttributes
                .builder("idOfTypicalCourse1", "Unreg Student", "unreg@stud.ent")
                .withSection("1")
                .withTeam("Team0.1")
                .withComments("asdf")
                .build();

        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        ______TS("not enough parameters");

        verifyAssumptionFailure();

        FeedbackSessionAttributes session1InCourse1 = dataBundle.feedbackSessions.get("session1InCourse1");

        String[] submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        verifyAssumptionFailure(submissionParams);

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        verifyAssumptionFailure(submissionParams);

        ______TS("Test null feedback session name parameter");

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        StudentFeedbackSubmissionEditPageAction pageAction;
        RedirectResult redirectResult;

        try {
            pageAction = getAction(submissionParams);
            redirectResult = getRedirectResult(pageAction);
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullHttpParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER,
                                       Const.ParamsNames.FEEDBACK_SESSION_NAME), e.getMessage());
        }

        ______TS("Test null course id parameter");

        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        try {
            pageAction = getAction(submissionParams);
            redirectResult = getRedirectResult(pageAction);
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullHttpParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER,
                                       Const.ParamsNames.COURSE_ID), e.getMessage());
        }

        ______TS("typical success case for registered student");

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        pageAction = getAction(submissionParams);
        ShowPageResult pageResult = getShowPageResult(pageAction);

        assertEquals(
                getPageResultDestination(
                        Const.ViewURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT,
                        false,
                        student1InCourse1.googleId),
                pageResult.getDestinationWithParams());
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_SUBMISSIONS_CAN_SUBMIT_PARTIAL_ANSWER, pageResult.getStatusMessage());

        ______TS("feedbacksession deleted");

        FeedbackSessionsDb feedbackSessionsDb = new FeedbackSessionsDb();

        feedbackSessionsDb.deleteEntity(session1InCourse1);

        String[] params = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        pageAction = getAction(params);
        redirectResult = getRedirectResult(pageAction);

        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, false, "student1InCourse1"),
                redirectResult.getDestinationWithParams());
        assertFalse(redirectResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_DELETED_NO_ACCESS,
                     redirectResult.getStatusMessage());

        // for unregistered student

        StudentsDb stDb = new StudentsDb();
        stDb.createStudent(unregStudent);
        unregStudent = stDb.getStudentForEmail("idOfTypicalCourse1", "unreg@stud.ent");
        gaeSimulation.logoutUser();

        params = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.REGKEY, StringHelper.encrypt(unregStudent.key),
                Const.ParamsNames.STUDENT_EMAIL, unregStudent.email
        };

        try {
            pageAction = getAction(params);

            AssertHelper.assertLogMessageEqualsForUnregisteredStudentUser(
                            "TEAMMATESLOG|||studentFeedbackSubmissionEditPage|||studentFeedbackSubmissionEditPage|||"
                          + "true|||Unregistered:idOfTypicalCourse1|||Unreg Student|||Unknown|||unreg@stud.ent|||"
                          + "Unknown|||/page/studentFeedbackSubmissionEditPage", pageAction.getLogMessage(),
                            unregStudent.email, unregStudent.course);

            redirectResult = getRedirectResult(pageAction);
            signalFailureToDetectException("EntityDoesNotExist");
        } catch (EntityNotFoundException enfe) {
            assertEquals("unregistered student trying to access non-existent session", enfe.getMessage());
        }

        stDb.deleteStudent("idOfTypicalCourse1", "unreg@stud.ent");

        ______TS("typical success case for unregistered student");

        gaeSimulation.loginAsStudent(student1InCourse1.googleId);
        removeAndRestoreTypicalDataBundle();

        session1InCourse1 = dataBundle.feedbackSessions.get("session1InCourse1");

        params = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session1InCourse1.getFeedbackSessionName(),
                Const.ParamsNames.USER_ID, student1InCourse1.googleId
        };

        pageAction = getAction(params);
        pageResult = getShowPageResult(pageAction);

        assertEquals(
                getPageResultDestination(
                        Const.ViewURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT, false, student1InCourse1.googleId),
                pageResult.getDestinationWithParams());
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_SUBMISSIONS_CAN_SUBMIT_PARTIAL_ANSWER, pageResult.getStatusMessage());

        ______TS("masquerade mode");

        gaeSimulation.loginAsAdmin("admin.user");

        pageAction = getAction(params);
        pageResult = getShowPageResult(pageAction);

        assertEquals(
                getPageResultDestination(
                        Const.ViewURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT, false, student1InCourse1.googleId),
                pageResult.getDestinationWithParams());
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_SUBMISSIONS_CAN_SUBMIT_PARTIAL_ANSWER, pageResult.getStatusMessage());

        ______TS("student has not joined course");

        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        student1InCourse1.googleId = null;

        new StudentsDb()
                .updateStudent(student1InCourse1.course, student1InCourse1.email,
                                                   student1InCourse1.name, student1InCourse1.team,
                                                   student1InCourse1.section, student1InCourse1.email,
                                                   student1InCourse1.googleId, student1InCourse1.comments);

        pageAction = getAction(params);
        redirectResult = getRedirectResult(pageAction);

        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, true, "student1InCourse1"),
                redirectResult.getDestinationWithParams());
        assertTrue(redirectResult.isError);
        assertEquals("You are not registered in the course " + session1InCourse1.getCourseId(),
                     redirectResult.getStatusMessage());
    }

    @Override
    protected StudentFeedbackSubmissionEditPageAction getAction(String... params) {
        return (StudentFeedbackSubmissionEditPageAction) gaeSimulation.getActionObject(getActionUri(), params);
    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        FeedbackSessionAttributes session1InCourse1 = dataBundle.feedbackSessions
                .get("session1InCourse1");

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.getFeedbackSessionName()
        };

        verifyOnlyStudentsOfTheSameCourseCanAccess(submissionParams);
    }
}
