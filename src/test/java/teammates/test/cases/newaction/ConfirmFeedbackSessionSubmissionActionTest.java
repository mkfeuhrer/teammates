package teammates.test.cases.newaction;

import org.testng.annotations.Test;

import teammates.common.util.Const;
import teammates.ui.newcontroller.ConfirmFeedbackSessionSubmissionAction;

/**
 * SUT: {@link ConfirmFeedbackSessionSubmissionAction}.
 */
public class ConfirmFeedbackSessionSubmissionActionTest extends BaseActionTest<ConfirmFeedbackSessionSubmissionAction> {

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.SUBMISSION_CONFIRMATION;
    }

    @Override
    protected String getRequestMethod() {
        return POST;
    }

    @Test
    @Override
    protected void testExecute() throws Exception {
        // TODO
    }

    @Test
    @Override
    protected void testAccessControl() throws Exception {
        // TODO
    }

}
