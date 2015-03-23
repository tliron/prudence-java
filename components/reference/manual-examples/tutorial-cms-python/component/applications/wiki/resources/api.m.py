import simplejson

def handle_init(conversation):
    conversation.addMediaTypeByName('application/json')
    id = 'page.' + conversation.wildcard
    caching.duration = 60000
    caching.tags.add(id) 

def handle_get(conversation):
    id = 'page.' + conversation.wildcard
    content = application.globals[id] or 'This page is empty.'
    return simplejson.dumps({'content': content})

def handle_put(conversation):
    id = 'page.' + conversation.wildcard
    payload = simplejson.loads(conversation.entity.text)
    application.globals[id] = payload['content']
    document.cache.invalidate(id)
    return simplejson.dumps({'content': payload['content']})

def handle_delete(conversation):
    id = 'page.' + conversation.wildcard
    del application.globals[id]
    document.cache.invalidate(id)
    return None
