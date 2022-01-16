function errMaker()
    error("My error")
    return "Hi"
end
function retTable(e)
    return {1, 2, 3}
end
a, b = xpcall(errMaker, retTable)
print(b[1])