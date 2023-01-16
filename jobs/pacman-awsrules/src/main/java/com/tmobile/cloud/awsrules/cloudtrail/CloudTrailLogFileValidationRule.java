package com.tmobile.cloud.awsrules.cloudtrail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-cloudtrail-log-file-validation-enabled", desc = "This rule checks for AWS CloudTrail logfile validation enabled", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class CloudTrailLogFileValidationRule extends BasePolicy {

	private static final Logger logger = LoggerFactory.getLogger(CloudTrailLogFileValidationRule.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 **************Following are the Rule Parameters********* <br><br>
	 *
	 *ruleKey :check-cloudtrail-log-file-validation-enabled <br><br>
	 *
	 *threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 *severity : Enter the value of severity <br><br>
	 * 
	 *ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */
	@Override
	public PolicyResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========CheckForCloudTrailLogFileValidationEnabled started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

		if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
				ruleParam.get(PacmanRuleConstants.CATEGORY))) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		Optional<String> opt = Optional.ofNullable(resourceAttributes).map(resource -> checkValidation(resource));

		PolicyResult ruleResult = Optional.ofNullable(ruleParam).filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

		logger.debug("========CheckForCloudTrailLogFileValidationEnabled ended=========");
		return ruleResult;

	}

	/**
	 * 
	 * Checks the CloudTrail log file validation is enabled.
	 * 
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return string
	 * 
	 */
	private String checkValidation(Map<String, String> resourceAttributes) {

		String description = null;
		String isEnabled = resourceAttributes.get(PacmanRuleConstants.CLOUD_TRAIL_LOG_FILE_VALIDATION);

		if (StringUtils.isNullOrEmpty(isEnabled) || isEnabled.equals("false")) {
			description = "CloudTrail log file validation is disabled";
		}
		return description;
	}

	private static PolicyResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {

		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION, description);
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
		issueList.add(issue);
		annotation.put("issueDetails", issueList.toString());
		logger.debug("========CheckForCloudTrailLogFileValidationEnabled annotation {} :=========", annotation);
		return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);

	}

	@Override
	public String getHelpText() {

		return "Checks the CloudTrail log file validation is enabled.";
	}

}
