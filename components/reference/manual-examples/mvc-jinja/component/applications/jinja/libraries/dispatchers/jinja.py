import simplejson, urllib
from com.threecrickets.prudence.util import InternalRepresentation

def handle_init(conversation):
    conversation.addMediaTypeByName('text/html')

def handle_post(conversation):
    if not conversation.internal:
        return 404
    id = conversation.locals['com.threecrickets.prudence.dispatcher.id']
    if id[-1] == '/':
        id = id[0:-1]
    id += '.html'
    context = {}
    if conversation.entity:
        if conversation.entity.mediaType.name == 'application/internal':
            context = conversation.entity.object
        else:
            context = conversation.entity.text
            if context:
                context = simplejson.loads(context)
    payload = InternalRepresentation({
        'context': context,
        'uri': str(conversation.reference),
        'base_uri': str(conversation.reference.baseRef)})
    resource = document.internal('/jinja-template/' + urllib.quote(id, '') + '/', 'text/html')
    result = resource.post(payload)
    if not result:
        return 404
    return result.text
