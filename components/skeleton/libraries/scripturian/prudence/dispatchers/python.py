#
# This file is part of the Prudence Foundation Library
#
# Copyright 2009-2013 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.gnu.org/copyleft/lesser.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

resources = {}

document.executeOnce(application.globals['com.threecrickets.prudence.dispatcher.python.resources'])

def handle(conversation, method):
    id = conversation.locals['com.threecrickets.prudence.dispatcher.id']
    resource = resources.get(id, None)
    if resource is None:
        conversation.statusCode = 404
        return None
    method = getattr(resource, method, None)
    if method is None:
        conversation.statusCode = 405
        return None
    return method(conversation)

def handle_init(conversation):
    handle(conversation, 'handle_init')

def handle_get(conversation):
    return handle(conversation, 'handle_get')

def handle_get_info(conversation):
    return handle(conversation, 'handle_get_info')

def handle_post(conversation):
    return handle(conversation, 'handle_post')

def handle_put(conversation):
    return handle(conversation, 'handle_put')

def handle_delete(conversation):
    return handle(conversation, 'handle_delete')
