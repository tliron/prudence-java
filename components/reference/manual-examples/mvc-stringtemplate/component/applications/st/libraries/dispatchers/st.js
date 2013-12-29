document.require('/sincerity/objects/')

function handleInit(conversation) {
    conversation.addMediaTypeByName('text/html')
}

function handlePost(conversation) {
    if (!conversation.internal) {
        return 404
    }
    var id = String(conversation.locals.get('com.threecrickets.prudence.dispatcher.id'))
    if (id.endsWith('/')) {
        id = id.substring(0, id.length - 1)
    }
    var st = getDir().getInstanceOf(id)
    if (!Sincerity.Objects.exists(st)) {
        return 404
    }
    if (Sincerity.Objects.exists(conversation.entity)) {
        var context = conversation.entity.object
        if (Sincerity.Objects.exists(conversation.context)) {
            for (var key in context) {
                var value = context[key]
                if (Sincerity.Objects.isArray(value)) {
                    for (var v in value) {
                        st.add(key, value[v])
                    }
                }
                else {
                    st.add(key, value)
                }
            }
        }
    }
    return st.render()
}

function getDir() {
    var dir = new org.stringtemplate.v4.STRawGroupDir(application.root + '/libraries/views/')
    dir.delimiterStartChar = '$'
    dir.delimiterStopChar = '$'
    return dir
}
