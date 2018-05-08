
def cross211 = new File(basedir, "cross/2.11")
def cross212 = new File(basedir, "cross/2.12")
def cross213 = new File(basedir, "cross/2.13")

def src211 = new File(cross211, "src")
def src212 = new File(cross212, "src")
def src213 = new File(cross213, "src")

assert src211.exists()
assert src212.exists()
assert src213.exists()
