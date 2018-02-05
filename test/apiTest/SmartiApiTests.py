import names
import unittest
import json
import random
import argparse
import sys

from SmartiApiRequests import *

parser = argparse.ArgumentParser(description='Run Smarti 0.7.0 Api Tests')
parser.add_argument('--url', default='http://localhost:8080/', dest='url',
                    help='Set Smarti url (Defaul: http://localhost:8080/)')
parser.add_argument('--username', default='admin', dest='username',
                    help='Set Smarti login username. User must be an admin (Defaul: admin)')
parser.add_argument('--password', default='admin', dest='password',
                    help='Set Smarti login password. (Defaul: admin)')
parser.add_argument('--loglevel', default='error', dest='loglevel', choices=('info', 'warn', 'debug', 'error'),
                    help='Set log level. (Defaul: error)')
args = parser.parse_args()

admin_user = args.username
admin_password = args.password
admin = Authentification(admin_user, admin_password)
url = args.url

if not url.endswith('/'):
  url = url+'/'

smarti = SmartiRequests(url, args.loglevel)


class SmartiTests(unittest.TestCase):

  @classmethod
  def tearDownClass(cls):
    smarti.logger.info('\nRUN teardown ...')
    smarti.cleanup(admin)

  @classmethod
  def setUp(cls):
    smarti.logger.info('\nRUN setup ... ')
    smarti.cleanup(admin)

  def testClientWebservice(self):
    clientname = str(names.get_last_name()).lower()
    smarti.logger.info('Clientname for this test: '+clientname)
    body = {
        'defaultClient': False,
        'description': 'description',
        'name': clientname
    }
    config = json.loads('{"queryBuilder":[{"_class":"io.redlink.smarti.model.config.ComponentConfiguration","name":"conversationmlt","displayName":"conversationmlt","type":"conversationmlt","enabled":true,"unbound":false,"pageSize":3,"filter":["support_area"]},{"_class":"io.redlink.smarti.model.config.ComponentConfiguration","name":"conversationsearch","displayName":"conversationsearch","type":"conversationsearch","enabled":true,"unbound":false,"pageSize":3,"filter":["support_area"]}]}')
    username = 'TestUser'
    user = {
        "login": username,
        "roles": [],
        "clients": [],
        "profile": {
            "name": username,
            "email": username+'@'+username+'.com'
        }
    }

    smarti.sendRequest(smarti.getClient, SmartiRequests.GET_SUCCESS, admin)
    clientid = smarti.sendRequest(
        smarti.postClient, SmartiRequests.POST_SUCCESS, admin, body).json().get('id')
    smarti.sendRequest(smarti.getClientSingle,
                       SmartiRequests.GET_SUCCESS, admin, clientid)

    smarti.sendRequest(smarti.getClientConfig,
                       SmartiRequests.GET_SUCCESS, admin, clientid)
    smarti.sendRequest(smarti.postClientConfig,
                       SmartiRequests.POST_SUCCESS, admin, clientid, config)

    smarti.sendRequest(smarti.getClientToken,
                       SmartiRequests.GET_SUCCESS, admin, clientid)
    tokenobj = smarti.sendRequest(
        smarti.postClientToken, SmartiRequests.POST_SUCCESS, admin, clientid, {})
    token = tokenobj.json().get('id')
    smarti.sendRequest(smarti.updateClientToken, SmartiRequests.UPDATE_SUCCESS,
                       admin, clientid, token, tokenobj.json())

    smarti.sendRequest(smarti.getClientUser,
                       SmartiRequests.GET_SUCCESS, admin, clientid)
    smarti.sendRequest(smarti.postClientUser,
                       SmartiRequests.POST_SUCCESS, admin, clientid, user)
    smarti.sendRequest(smarti.updateClientUser,
                       SmartiRequests.UPDATE_SUCCESS, admin, clientid, username)

    smarti.sendRequest(smarti.delClientUser,
                       SmartiRequests.DELETE_SUCCESS, admin, clientid, username)
    smarti.sendRequest(smarti.delClientToken,
                       SmartiRequests.DELETE_SUCCESS, admin, clientid, token)
    smarti.sendRequest(
        smarti.delClient, SmartiRequests.DELETE_SUCCESS, admin, clientid)

  def testConversationWebservice(self):
    clientname = str(names.get_last_name()).lower()
    smarti.logger.info('Clientname for this test: '+clientname)
    body = {
        'defaultClient': False,
        'description': 'description',
        'name': clientname
    }
    config = json.loads('{"queryBuilder":[{"_class":"io.redlink.smarti.model.config.ComponentConfiguration","name":"conversationmlt","displayName":"conversationmlt","type":"conversationmlt","enabled":true,"unbound":false,"pageSize":3,"filter":["support_area"]},{"_class":"io.redlink.smarti.model.config.ComponentConfiguration","name":"conversationsearch","displayName":"conversationsearch","type":"conversationsearch","enabled":true,"unbound":false,"pageSize":3,"filter":["support_area"]}]}')
    clientid = ''
    token = ''
    conversationid = ''
    creator = ''
    analysis = {}
    message = {}
    messageid = ''
    mfieldname = 'votes'
    mfield = {
        'value': 1
    }
    cfieldname = 'context.domain'
    cfield = {
        'value': 'test'
    }

    clientid = smarti.sendRequest(
        smarti.postClient, SmartiRequests.POST_SUCCESS, admin, body).json().get('id')
    smarti.logger.info('ClientID for this test: '+clientid)
    smarti.sendRequest(smarti.postClientConfig,
                       SmartiRequests.POST_SUCCESS, admin, clientid, config)
    token = smarti.sendRequest(
        smarti.postClientToken, SmartiRequests.POST_SUCCESS, admin, clientid, {}).json().get('token')
    smarti.logger.info('Token for this test: '+token)
    user = Authentification(token)

    smarti.sendRequest(smarti.getConversation,
                       SmartiRequests.GET_SUCCESS, user, {})
    conversationid = smarti.sendRequest(
        smarti.postConversation, SmartiRequests.POST_SUCCESS, user, {}, {}).json().get('id')
    smarti.logger.info('ConversationID for this test: '+conversationid)
    smarti.sendRequest(smarti.getConversationSearch,
                       SmartiRequests.GET_SUCCESS, user, {})
    smarti.sendRequest(smarti.getConversationSingle,
                       SmartiRequests.GET_SUCCESS, user, conversationid, {})
    smarti.sendRequest(smarti.getConversationAnalysis,
                       SmartiRequests.GET_SUCCESS, user, conversationid, {})
    smarti.sendRequest(smarti.postConversationAnalaysis,
                       SmartiRequests.POST_SUCCESS, user, conversationid, {}, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplate,
                       SmartiRequests.GET_SUCCESS, user, conversationid, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplateSingle,
                       SmartiRequests.GET_SUCCESS, user, conversationid, '0', {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplateResult,
                       SmartiRequests.GET_SUCCESS, user, conversationid, '0', creator, {})
    smarti.sendRequest(smarti.postConversationAnalysisTemplateResult,
                       SmartiRequests.POST_SUCCESS, user, conversationid, '0', creator, analysis, {})
    smarti.sendRequest(smarti.getConversationAnalysisToken,
                       SmartiRequests.GET_SUCCESS, user, conversationid, {})
    smarti.sendRequest(smarti.getConversationMessage,
                       SmartiRequests.GET_SUCCESS, user, conversationid, {})
    messageid = smarti.sendRequest(smarti.postConversationMessage,
                                   SmartiRequests.POST_SUCCESS, user, conversationid, message, {}).json().get('id')
    smarti.logger.info('MessageID for this test: ' + messageid)
    smarti.sendRequest(smarti.getConversationMessageSingle,
                       SmartiRequests.GET_SUCCESS, user, conversationid, messageid, {})
    smarti.sendRequest(smarti.updateConversationMessage,
                       SmartiRequests.UPDATE_SUCCESS, user, conversationid, messageid, message, {})
    smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                       user, conversationid, messageid, mfieldname, mfield, {})
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       user, conversationid, cfieldname, cfield, {})
    smarti.sendRequest(smarti.delConversationMessage,
                       SmartiRequests.DELETE_SUCCESS, user, conversationid, messageid, {})
    smarti.sendRequest(smarti.delConversationField,
                       SmartiRequests.DELETE_SUCCESS, user, conversationid, cfieldname, {})

  def testAllUserRequests(self):
    username = 'TestUser'
    signupUser = {
        "login": username,
        "roles": []
    }
    user = {
        "login": username,
        "roles": [],
        "clients": [],
        "profile": {
            "name": username,
            "email": username+'@'+username+'.com'
        }
    }
    password = 'password'
    roles = []
    newPassword = 'newPassword'

    smarti.sendRequest(smarti.getAuth, SmartiRequests.GET_SUCCESS)
    smarti.sendRequest(smarti.getAuthCheck,
                       SmartiRequests.GET_SUCCESS, admin_user)
    smarti.sendRequest(smarti.postAuthRecovery,
                       SmartiRequests.POST_SUCCESS, admin_user, {})
    smarti.sendRequest(smarti.postAuthSignup,
                       SmartiRequests.POST_SUCCESS, signupUser)
    smarti.sendRequest(smarti.getUser, SmartiRequests.GET_SUCCESS, admin, {})
    smarti.sendRequest(
        smarti.postUser, SmartiRequests.POST_SUCCESS, admin, user)
    smarti.sendRequest(smarti.getUserSingle,
                       SmartiRequests.GET_SUCCESS, admin, username)
    smarti.sendRequest(
        smarti.updateUser, SmartiRequests.UPDATE_SUCCESS, admin, username, user)
    smarti.sendRequest(smarti.updateUserPassword, SmartiRequests.UPDATE_SUCCESS,
                       admin, username, {"password": newPassword})
    smarti.sendRequest(smarti.updateUserRoles,
                       SmartiRequests.UPDATE_SUCCESS, admin, username, {"roles": roles})
    smarti.sendRequest(
        smarti.delUser, SmartiRequests.DELETE_SUCCESS, admin, username)

  def testFull(self):
    messageNum = 3
    clientname = str(names.get_last_name()).lower()
    smarti.logger.info('Clientname for this test: '+clientname)
    body = {
        'defaultClient': False,
        'description': 'description',
        'name': clientname
    }
    config = json.loads('{"queryBuilder":[{"_class":"io.redlink.smarti.model.config.ComponentConfiguration","name":"conversationmlt","displayName":"conversationmlt","type":"conversationmlt","enabled":true,"unbound":false,"pageSize":3,"filter":["support_area"]},{"_class":"io.redlink.smarti.model.config.ComponentConfiguration","name":"conversationsearch","displayName":"conversationsearch","type":"conversationsearch","enabled":true,"unbound":false,"pageSize":3,"filter":["support_area"]}]}')
    clientid = ''
    token = ''
    creator = 'queryBuilder:conversationmlt:conversationmlt'
    analysis = {}
    message = {}
    messageid = ''
    mfieldname = 'votes'
    mfield = {
        'value': 1
    }
    cfieldname = 'context.domain'
    cfield = {
        'value': 'test'
    }
    username = names.get_first_name().lower().strip()
    password = username
    user = {
        "login": username,
        "roles": [],
        "clients": [],
        "profile": {
            "name": username,
            "email": username+'@'+username+'.com',
        }
    }

    clientid = smarti.sendRequest(
        smarti.postClient, SmartiRequests.POST_SUCCESS, admin, body).json().get('id')
    smarti.logger.info('ClientID for this test: '+clientid)
    smarti.sendRequest(smarti.postClientConfig,
                       SmartiRequests.POST_SUCCESS, admin, clientid, config)
    token = smarti.sendRequest(
        smarti.postClientToken, SmartiRequests.POST_SUCCESS, admin, clientid, {}).json().get('token')
    smarti.logger.info('Token for this test: '+token)
    tokenauth = Authentification(token)
    smarti.sendRequest(
        smarti.postUser, SmartiRequests.POST_SUCCESS, admin, user)
    smarti.sendRequest(smarti.updateUserPassword, SmartiRequests.UPDATE_SUCCESS,
                       admin, username, {"password": password})
    smarti.sendRequest(smarti.updateUserRoles,
                       SmartiRequests.UPDATE_SUCCESS, admin, username, ["USER"])
    smarti.logger.info('Username for this test: '+username)
    userauth = Authentification(username, password)
    smarti.sendRequest(smarti.updateClientUser,
                       SmartiRequests.UPDATE_SUCCESS, admin, clientid, username)

    # Create Conversation via token
    conversationid1 = smarti.sendRequest(
        smarti.postConversation, SmartiRequests.POST_SUCCESS, tokenauth, {}, {}).json().get('id')
    smarti.logger.info(
        'ConversationID for token based authentification in this test: '+conversationid1)
    smarti.sendRequest(smarti.postConversationMessage, SmartiRequests.POST_SUCCESS,
                       tokenauth, conversationid1, smarti.randomMessage(), {})
    smarti.sendRequest(smarti.postConversationMessage, SmartiRequests.POST_SUCCESS,
                       tokenauth, conversationid1, smarti.randomMessage(), {})
    smarti.sendRequest(smarti.postConversationMessage, SmartiRequests.POST_SUCCESS,
                       tokenauth, conversationid1, smarti.randomMessage(), {})
    smarti.sendRequest(smarti.getConversationAnalysis,
                       SmartiRequests.GET_SUCCESS, tokenauth, conversationid1, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplate,
                       SmartiRequests.GET_SUCCESS, tokenauth, conversationid1, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplateResult,
                       SmartiRequests.GET_SUCCESS, tokenauth, conversationid1, '0', creator, {})

    # Create Conversation via user
    conversationid2 = smarti.sendRequest(
        smarti.postConversation, SmartiRequests.POST_SUCCESS, userauth, {}, {}).json().get('id')
    smarti.logger.info(
        'ConversationID for user based authentification in this test: '+conversationid2)
    smarti.sendRequest(smarti.postConversationMessage, SmartiRequests.POST_SUCCESS,
                       userauth, conversationid2, smarti.randomMessage(), {})
    smarti.sendRequest(smarti.postConversationMessage, SmartiRequests.POST_SUCCESS,
                       userauth, conversationid2, smarti.randomMessage(), {})
    smarti.sendRequest(smarti.postConversationMessage, SmartiRequests.POST_SUCCESS,
                       userauth, conversationid2, smarti.randomMessage(), {})
    smarti.sendRequest(smarti.getConversationAnalysis,
                       SmartiRequests.GET_SUCCESS, userauth, conversationid2, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplate,
                       SmartiRequests.GET_SUCCESS, userauth, conversationid2, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplateResult,
                       SmartiRequests.GET_SUCCESS, userauth, conversationid2, '0', creator, {})

    # Publish first conversation
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       tokenauth, conversationid1, 'meta.status', "Complete", {})
    smarti.sendRequest(smarti.getConversationAnalysis,
                       SmartiRequests.GET_SUCCESS, tokenauth, conversationid1, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplate,
                       SmartiRequests.GET_SUCCESS, tokenauth, conversationid1, {})
    smarti.sendRequest(smarti.getConversationAnalysisTemplateResult,
                       SmartiRequests.GET_SUCCESS, tokenauth, conversationid1, '0', creator, {})

    # Edit second conversation
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       tokenauth, conversationid2, 'context.contextType', "application/json", {})
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       tokenauth, conversationid2, 'context.domain', "testdomain", {})
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       tokenauth, conversationid2, 'context.environment.*', "value", {})
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       tokenauth, conversationid2, 'meta.status', "Complete", {})
    smarti.sendRequest(smarti.updateConversationField, SmartiRequests.UPDATE_SUCCESS,
                       tokenauth, conversationid2, 'meta.*', "value", {})

    smarti.sendRequest(smarti.delConversationField, SmartiRequests.DELETE_SUCCESS,
                       userauth, conversationid2, 'context.domain', {})
    smarti.sendRequest(smarti.delConversationField, SmartiRequests.DELETE_SUCCESS,
                       userauth, conversationid2, 'context.environment.*', {})
    smarti.sendRequest(smarti.delConversationField, SmartiRequests.DELETE_SUCCESS,
                       userauth, conversationid2, 'context.contextType', {})
    smarti.sendRequest(smarti.delConversationField, SmartiRequests.DELETE_SUCCESS,
                       userauth, conversationid2, 'meta.status', {})
    smarti.sendRequest(smarti.delConversationField,
                       SmartiRequests.DELETE_SUCCESS, userauth, conversationid2, 'meta.*', {})

    # Edit first message
    backupMessage = smarti.sendRequest(
        smarti.getConversationMessage, SmartiRequests.GET_SUCCESS, userauth, conversationid2, {}).json()[0]
    messageid = backupMessage.get('id')
    smarti.logger.info(
        'MessageID of Message to be modified in this test: '+messageid)
    smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                       userauth, conversationid2, messageid, 'time', 1516888011317, {})
    smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                       userauth, conversationid2, messageid, 'origin', 'User', {})
    smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                       userauth, conversationid2, messageid, 'content', 'content', {})
    print(smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                             userauth, conversationid2, messageid, 'private', 'true', {}).text)
    smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                       userauth, conversationid2, messageid, 'votes', '2', {})
    smarti.sendRequest(smarti.updateConversationMessageField, SmartiRequests.UPDATE_SUCCESS,
                       userauth, conversationid2, messageid, 'metadata.*', {"key": "value"}, {})

    # Reset message to origin
    smarti.sendRequest(smarti.updateConversationMessage, SmartiRequests.UPDATE_SUCCESS,
                       userauth, conversationid2, messageid, backupMessage, {})

    # Delete message
    smarti.sendRequest(smarti.delConversationMessage,
                       SmartiRequests.DELETE_SUCCESS, userauth, conversationid2, messageid, {})
    smarti.sendRequest(smarti.getConversationMessage,
                       SmartiRequests.GET_SUCCESS, userauth, conversationid2, {})


if __name__ == '__main__':
  unittest.main(argv=sys.argv[:1])
